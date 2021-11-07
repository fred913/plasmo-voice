/*
 * This file is licensed under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * Because it contains modified code from:
 * https://github.com/RestComm/media-core/blob/master/rtp/src/main/java/org/restcomm/media/core/rtp/jitter/FixedJitterBuffer.java
 */

package su.plo.voice.client.sound;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.sound.clock.RtpClock;
import su.plo.voice.protocol.packets.udp.MessageUdp;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FixedJitterBuffer {
    // The underlying buffer size
    private static final int QUEUE_SIZE = 10;
    // the underlying buffer
    private final ArrayList<MessageUdp> queue = new ArrayList<>(QUEUE_SIZE);

    // allowed jitter
    private final long jitterBufferSize;

    // RTP clock
    private final RtpClock rtpClock;

    private long sequenceNumber = -1L;

    /**
     * used to calculate network jitter. currentTransit measures the relative time it takes for an RTP packet to arrive from the
     * remote server to MMS
     */
    private long currentTransit = 0;

    /**
     * continuously updated value of network jitter
     */
    private long currentJitter = 0;

    // packet arrival dead line measured on RTP clock.
    // initial value equals to infinity
    private long arrivalDeadLine = 0;

    // packet arrival dead line measured on RTP clock.
    // initial value equals to infinity
    private long droppedInRaw = 0;

    // The number of dropped packets
    private int dropCount;

    // known duration of media wich contains in this buffer.
    private volatile long duration;

    private final AtomicBoolean ready;

    private final Lock lock = new ReentrantLock();

    public FixedJitterBuffer(RtpClock rtpClock, int jitterBufferSize) {
        this.rtpClock = rtpClock;
        this.jitterBufferSize = jitterBufferSize;
        this.ready = new AtomicBoolean(false);
    }

    private void initJitter(MessageUdp packet) {
        long arrival = System.currentTimeMillis();
        long firstPacketTimestamp = packet.getTimestamp();
        currentTransit = arrival - firstPacketTimestamp;
    }

    /**
     * Calculates the current network jitter
     */
    private void estimateJitter(MessageUdp packet) {
        long arrival = System.currentTimeMillis();
        long newPacketTimestamp = packet.getTimestamp();
        long transit = arrival - newPacketTimestamp;
        long d = transit - currentTransit;
        if (d < 0) {
            d = -d;
        }

        currentTransit = transit;
        currentJitter += d - ((currentJitter + 8) >> 4);
    }

    /**
     *
     * @return the current value of the network jitter
     */
    public long getEstimatedJitter() {
        return currentJitter >> 4;
    }

    /**
     * Get the number of dropped packets.
     *
     * @return the number of dropped packets.
     */
    public int getDropped() {
        return dropCount;
    }

    private void writeSync(MessageUdp packet) {
        // if this is first packet then synchronize clock
        if (sequenceNumber == -1) {
            rtpClock.synchronize(packet.getTimestamp());
            this.sequenceNumber = packet.getSequenceNumber();
            initJitter(packet);
        } else {
            estimateJitter(packet);
        }

        // update clock rate
        rtpClock.setClockRate(AudioCapture.getSampleRate());

        // drop outstanding packets
        // packet is outstanding if its timestamp of arrived packet is less
        // then consumer media time
        if (rtpClock.convertToRtpTime(packet.getTimestamp()) < this.arrivalDeadLine) {
            VoiceClient.LOGGER.debug("drop packet: dead line={}, packet time={}, sequence={}, payload length={}, format={}",
                    arrivalDeadLine, packet.getTimestamp(), packet.getSequenceNumber(), packet.getPayloadLength(),
                    AudioCapture.getFormat().toString());
            dropCount++;

            // checking if not dropping too much
            droppedInRaw++;
            if (droppedInRaw == QUEUE_SIZE / 2 || queue.size() == 0) {
                arrivalDeadLine = 0;
            } else {
                return;
            }
        }


        droppedInRaw = 0;

        // find correct position to insert a packet
        // use timestamp since its always positive
        int currIndex = queue.size() - 1;
        while (currIndex >= 0 && queue.get(currIndex).getTimestamp() > packet.getTimestamp()) {
            currIndex--;
        }

        // check for duplicate packet
        if (currIndex >= 0 && queue.get(currIndex).getSequenceNumber() == packet.getSequenceNumber()) {
            return;
        }

        queue.add(currIndex + 1, packet);

        // recalculate duration of each frame in queue and overall duration
        // since we could insert the frame in the middle of the queue
        duration = 0;
        if (queue.size() > 1) {
            duration = queue.get(queue.size() - 1).getTimestamp() - queue.get(0).getTimestamp();
        }

        for (int i = 0; i < queue.size() - 1; i++) {
            // duration measured by wall clock
            long d = queue.get(i + 1).getTimestamp() - queue.get(i).getTimestamp();
            // in case of RFC2833 event timestamp remains same
            queue.get(i).setDuration(d > 0 ? d : 0);
        }

        // if overall duration is negative we have some mess here,try to
        // reset
        if (duration < 0 && queue.size() > 1) {
            VoiceClient.LOGGER.warn("Something messy happened. Resetting jitter buffer!");
            reset();
            return;
        }

        // overflow?
        // only now remove packet if overflow , possibly the same packet we just received
        if (queue.size() > QUEUE_SIZE) {
            if (VoiceClient.LOGGER.isTraceEnabled()) {
                VoiceClient.LOGGER.trace("Jitter Buffer overflow! (duration={}ms, packets={})", duration, queue.size());
            }
            dropCount++;
            queue.remove(0);
        }

        // check if this buffer already full
        boolean readyTest = duration >= jitterBufferSize && queue.size() > 1;
        if (ready.compareAndSet(false, readyTest)) {
            if (ready.get()) {
                VoiceClient.LOGGER.debug("Jitter Buffer is ready! (duration={}ms, packets={})", duration, queue.size());
            }
        }
    }

    /**
     * Accepts specified packet
     *
     * @param packet the packet to accept
     */
    public void write(MessageUdp packet) {
        boolean locked = false;
        try {
            locked = this.lock.tryLock() || this.lock.tryLock(5, TimeUnit.MILLISECONDS);
            if (locked) {
                writeSync(packet);
            }
        } catch (InterruptedException e) {
            if (VoiceClient.LOGGER.isTraceEnabled()) {
                VoiceClient.LOGGER.trace("Could not acquire write lock for jitter buffer. Dropped packet.");
            }
        } finally {
            if (locked) {
                this.lock.unlock();
            }
        }
    }

    /**
     * Polls packet from buffer's head.
     *
     * @return the udp packet
     */
    public MessageUdp read() {
        MessageUdp packet = null;
        boolean locked = false;
        try {
            locked = this.lock.tryLock() || this.lock.tryLock(5, TimeUnit.MILLISECONDS);
            if (locked) {
                packet = safeRead();
            } else {
                this.ready.set(false);
            }
        } catch (InterruptedException e) {
            if (VoiceClient.LOGGER.isTraceEnabled()) {
                VoiceClient.LOGGER.trace("Could not acquire reading lock for jitter buffer.");
            }
            this.ready.set(false);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        return packet;
    }

    private MessageUdp safeRead() {
        if (queue.size() == 0) {
            this.ready.set(false);
            if (VoiceClient.LOGGER.isTraceEnabled()) {
                VoiceClient.LOGGER.trace("Jitter Buffer is empty. Consumer will wait until buffer is filled.");
            }
            return null;
        }

        // extract packet
        MessageUdp message = queue.remove(0);

        // buffer empty now? - change ready flag.
        if (queue.size() == 0) {
            this.ready.set(false);
            if (VoiceClient.LOGGER.isTraceEnabled()) {
                VoiceClient.LOGGER.trace("Read last packet from Jitter Buffer.");
            }
            // arrivalDeadLine = 0;
            // set it as 1 ms since otherwise will be dropped by pipe
            message.setDuration(1);
        }

        arrivalDeadLine = rtpClock.convertToRtpTime(message.getTimestamp() + message.getDuration());

        // convert duration to nanoseconds
        message.setDuration(message.getDuration() * 1000000L);
        message.setTimestamp(message.getTimestamp() * 1000000L);

        return message;
    }

    /**
     * Resets buffer.
     */
    public void reset() {
        boolean locked = false;
        try {
            locked = lock.tryLock() || lock.tryLock(5, TimeUnit.MILLISECONDS);
            if (locked) {
                queue.clear();
            }
        } catch (InterruptedException e) {
            if (VoiceClient.LOGGER.isTraceEnabled()) {
                VoiceClient.LOGGER.trace("Could not acquire lock to reset jitter buffer.");
            }
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    public void restart() {
        reset();
        this.ready.set(false);
        arrivalDeadLine = 0;
        dropCount = 0;
        droppedInRaw = 0;
        sequenceNumber = -1;

        if (VoiceClient.LOGGER.isDebugEnabled()) {
            VoiceClient.LOGGER.debug("Restarted jitter buffer.");
        }
    }
}
