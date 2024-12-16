package net.mahtabalam.entity;

import java.time.LocalDateTime;

public class Message {
   private long id;
   private long senderId;
   private long recipientId;
   private String message;
   private LocalDateTime createdAt;
   private LocalDateTime editedAt;
   private LocalDateTime deletedAt;
   
	public Message(long id, long senderId, long recipientId, String message, LocalDateTime createdAt,
			LocalDateTime editedAt, LocalDateTime deletedAt) {
		this.id = id;
		this.senderId = senderId;
		this.recipientId = recipientId;
		this.message = message;
		this.createdAt = createdAt;
		this.editedAt = editedAt;
		this.deletedAt = deletedAt;
	}

	public long getId() {
		return id;
	}

	public long getSenderId() {
		return senderId;
	}

	public long getRecipientId() {
		return recipientId;
	}

	public String getMessage() {
		return message;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getEditedAt() {
		return editedAt;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", senderId=" + senderId + ", recipientId=" + recipientId + ", message=" + message
				+ ", createdAt=" + createdAt + ", editedAt=" + editedAt + ", deletedAt=" + deletedAt + "]";
	}
   
   
}
