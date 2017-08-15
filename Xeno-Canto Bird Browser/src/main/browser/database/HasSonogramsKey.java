package main.browser.database;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class HasSonogramsKey implements Comparable<HasSonogramsKey> {
	final String recordingId;
	final long onsetPreferenceId;
	final long sonogramPreferenceId;
	
	public HasSonogramsKey(String rid, long opd, long spd) {
		recordingId = rid;
		onsetPreferenceId = opd;
		sonogramPreferenceId = spd;
	}
	
	@Override
	public int compareTo(HasSonogramsKey o) {
		if (equals(o))
			return 0;
		int c = recordingId.compareTo(o.recordingId);
		if (c != 0)
			return c;
		if (onsetPreferenceId < o.onsetPreferenceId)
			return -1;
		if (onsetPreferenceId > o.onsetPreferenceId)
			return 1;
		if (sonogramPreferenceId < o.sonogramPreferenceId)
			return -1;
		if (sonogramPreferenceId > o.sonogramPreferenceId)
			return 1;
		return 0;
	}

    @Override
    public int hashCode() {
        return new HashCodeBuilder(929, 2677). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(recordingId).
            append(onsetPreferenceId).
            append(sonogramPreferenceId).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof HasSonogramsKey))
            return false;
        if (obj == this)
            return true;

        HasSonogramsKey rhs = (HasSonogramsKey) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(recordingId, rhs.recordingId).
            append(onsetPreferenceId, rhs.onsetPreferenceId).
            append(sonogramPreferenceId, rhs.sonogramPreferenceId).
            isEquals();
    }

}
