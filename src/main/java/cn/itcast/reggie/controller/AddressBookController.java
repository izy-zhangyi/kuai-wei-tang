package cn.itcast.reggie.controller;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.common.ReggieContext;
import cn.itcast.reggie.domain.AddressBook;
import cn.itcast.reggie.service.AddressBookService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 当前用户新增地址信息
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> saveAddressBook(@RequestBody AddressBook addressBook) {
        //新增地址: 当前用户新增地址信息
        // ，通过用户id将地址信息以用户id的形式保存数据库
        /**
         * 将用户id放入TreadLocal中临时保存，用完之后还要移除
         */
        addressBook.setUserId(ReggieContext.get());
        //日志打印新增地址信息
        log.info("addressBook:{}", addressBook);
        //调用接口中方法，将新增地址保存
        this.addressBookService.save(addressBook);
        return R.success("新增地址成功");
    }

    @PostMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        //先看一下要修的地址，日志打印 被修改的地址信息系
        log.info("addressBook:{}",addressBook);
        //之后，修改地址默认信息，条件构造器
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        //根据id修改信息,默认地址信息唯一，先将所有的地址 设置 为全不是默认的
        updateWrapper.eq(AddressBook::getId,ReggieContext.get()).set(AddressBook::getIsDefault,0);

        //修改之后，调用接口中的方法，执行修改，将修改的值进行修改处理
        this.addressBookService.update(updateWrapper);

        //之后设置默认地址状态，
        addressBook.setIsDefault(1);
        //最后，再次调用接口中的放法，执行修改,修改时 通过用户id信息进行修改数据
        this.addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

}
