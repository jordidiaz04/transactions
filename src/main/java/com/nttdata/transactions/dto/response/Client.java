package com.nttdata.transactions.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private String id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private int type;
    private int profile;
}
