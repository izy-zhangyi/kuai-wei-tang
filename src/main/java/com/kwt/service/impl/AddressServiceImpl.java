package com.kwt.service.impl;

import com.kwt.domain.AddressBook;
import com.kwt.mapper.AddressBookMapper;
import com.kwt.service.AddressBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
