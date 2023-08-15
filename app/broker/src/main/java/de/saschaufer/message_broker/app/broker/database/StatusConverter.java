package de.saschaufer.message_broker.app.broker.database;

import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.EnumWriteSupport;

@WritingConverter
public class StatusConverter extends EnumWriteSupport<Message.Status> {
}
