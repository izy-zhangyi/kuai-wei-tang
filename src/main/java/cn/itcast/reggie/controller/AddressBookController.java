package cn.itcast.reggie.controller;

import cn.itcast.reggie.common.R;
import cn.itcast.reggie.common.ReggieContext;
import cn.itcast.reggie.domain.AddressBook;
import cn.itcast.reggie.service.AddressBookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 当前用户新增地址信息
     *
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

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        //先看一下要修的地址，日志打印 被修改的地址信息系
        log.info("addressBook:{}", addressBook);
        //之后，修改地址默认信息，条件构造器
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        //根据id修改信息,默认地址信息唯一，先将所有的地址 设置 为全不是默认的
        updateWrapper.eq(AddressBook::getId, ReggieContext.get()).set(AddressBook::getIsDefault, 0);

        //修改之后，调用接口中的方法，执行修改，将修改的值进行修改处理
        this.addressBookService.update(updateWrapper);

        //之后设置默认地址状态，
        addressBook.setIsDefault(1);
        //最后，再次调用接口中的放法，执行修改,修改时 通过用户id信息进行修改数据
        this.addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 获取默认地址，或者是查询默认地址
     *
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        /**
         * 创建查询构造器
         */
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //通过用户id，获取所需地址信息
        // 查询到用户所对应的所有地址信息,只获取默认的地址信息
        queryWrapper.eq(AddressBook::getUserId, ReggieContext.get()).eq(AddressBook::getIsDefault, 1);
        // 只获取默认的地址信息
        /*queryWrapper.eq(AddressBook::getIsDefault,1);*/
        //调用接口中的方法，将获取的默认地址信息封装成对象(默认地址唯一)
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        //将封装的地址信息的对象作为数据返回
        if (addressBook == null) {
            return R.error("没有找到该用户设置的默认地址");
        }
        return R.success(addressBook);
    }

    /**
     * 根据id查询地址信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook == null) {
            return R.error("该地址不存在");
        }
        return R.success(addressBook);
    }

    /**
     * 获取当前用户的所有地址信息
     *
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        // 获取登录用户的id信息
        addressBook.setUserId(ReggieContext.get());
        log.info("addressBook:{}", addressBook);

        //构造 天剑构造器，通过获取的用户id，来获取该用户下的所有地址信息
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //首先，用户id不可为空，其次方可将地址表中的用户所对应的id与登录的用户id匹配
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> addressBooks = addressBookService.list(queryWrapper);
        return R.success(addressBooks);


    }
}
