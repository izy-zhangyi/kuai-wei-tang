package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.AddressBook;
import cn.itcast.reggie.mapper.AddressBookMapper;
import cn.itcast.reggie.service.AddressBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
