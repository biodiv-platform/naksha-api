package com.strandls.naksha.utils;

public class ChunkOffsetConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final long expectedOffset;

	public ChunkOffsetConflictException(long expectedOffset) {
		super("Offset conflict, expected offset: " + expectedOffset);
		this.expectedOffset = expectedOffset;
	}

	public long getExpectedOffset() {
		return expectedOffset;
	}

}