# Remove the table since the messages waiting for ACK are only needed while the application is running.

drop table message_waiting_for_acknowledgement;
