package cn.itcast.reggie.service.impl;

import cn.itcast.reggie.domain.Category;
import cn.itcast.reggie.domain.Dish;
import cn.itcast.reggie.domain.DishFlavor;
import cn.itcast.reggie.dto.DishDto;
import cn.itcast.reggie.exception.BusinessException;
import cn.itcast.reggie.mapper.DishFlavorMapper;
import cn.itcast.reggie.mapper.DishMapper;
import cn.itcast.reggie.service.CategoryService;
import cn.itcast.reggie.service.DishFlavorService;
import cn.itcast.reggie.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private CategoryService categoryService;


    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时添加对应口味
     *
     * @param dishDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDto dishDto) {
        //1、新增菜品，
        // 手动生成id（用mybatisPlus中的方法可手动生成）
        long id = IdWorker.getId();
        dishDto.setId(id);//将id存入DishDto中
        this.save(dishDto);//利用此方法中的save保存菜品的基本信息到 Dish 表中

        //1.2、设置菜品口味和菜品的关系
        // 就是菜品口味的dishId与菜品的id
        dishDto.getFlavors().forEach(dishFlavor -> {
            //因为调用过save方法后，id会自动写入到对象中
            dishFlavor.setDishId(id);
        });

        // 2、新增菜品口味
        //用service的原因就是mapper里面没有封装 批量新增的方法
        this.dishFlavorService.saveBatch(dishDto.getFlavors());//批量添加菜品口味到Dish_flavor表中
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<DishDto> pageDishByXML(Integer page, Integer pageSize, String name) {
        /**
         * 1-- 查询出来的实体类
         */
        //1、创建 Dish 分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);

        //1.2、查询构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //1.3、添加过滤条件
        queryWrapper.like(StringUtils.isNotBlank(name), Dish::getName, name);

        //1.4、添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        this.page(dishPage, queryWrapper);

        /**
         *  2 主要核心就是构造一个 DishDto 的分页对象，就是因为 dto 中有一个 categoryName
         */
        // 2、创建 DishDto 分页构造器对象,就是因为 dto 中有一个 categoryName
        Page<DishDto> dishDtoPage = new Page<>();

        /**
         * 2.1
         * 拷贝Dish 的 page 分页对象数据到 DishDto 的page分页对象数据中去
         */
        BeanUtils.copyProperties(dishPage, dishDtoPage);

        /**
         * 拷贝完分页数据之后，dishDtoPage中就已经有了 DishPage 分页的数据
         * 等拷贝完 Dish 中的所有内容之后，dishDto 中就已经封装了所有的拷贝的数据内容了
         * then，
         * 获取DishDto 的 list 对象，即是为了后面方便填充数据
         * 获取 DishDto 的list对象，就可以看到所有 拷贝的 Dish 中的数据
         * status：虽然拷贝拿到了Dish中的page数据，但是 DishDto 里面没有内容
         * ，只有等 获取到 dish集合中的数据，就可以 填充到 DishDto中了
         */
        List<DishDto> dishDtoList = new ArrayList<>();


        // 这就可以拿到 查询出来的 实体类的 list
        List<Dish> dishList = dishPage.getRecords();
        /**
         * 为了避免在循环之中操作数据库
         * 就需要提前将所有 对应的 分类查查出来
         *      先拿到所有分类的 id
         * 为了避免拿到的id值重复，用toSet转换，
         * 之后就可以拿到菜品的list
         */
        Set<Long>  categoryId = dishList.stream().map(Dish::getCategoryId).collect(Collectors.toSet());
        //根据拿到的id，就可以查到所有的 category （即是拿到了所有的分类的list）
        List<Category> categoryList = categoryService.listByIds(categoryId);
        //方法3：使用前：list 要转 map toMap(key,value), 之后就可以dishList循环去拿
        Map<Long, Category> categoryMap = categoryList.stream().collect(Collectors.toMap(Category::getId, Function.identity()));
        /**
         * 拿到这两个list，就要想办法将这两个list匹配上
         * 三种在内存中操作方法：
         * 方法1：双重for循环，判断匹配
         * 方法2：jdk8操作流程
         * 方法3：使用前：list 要转 map toMap(key,value)
         * 性能排序：3 > 2 > 1
         */

        // 方法1：双重for循环匹配( 菜品的 list 与 分类的 list 循环匹配 )
          // 通过两个list中的id匹配
/*
        for (Dish dish : dishList) {
            */
/**
             *  拿到 dish 中的所有数据之后，就可以拷贝数据了：
             *  将 dishList 中的数据 拷贝到 dishDtoList 的集合中
             *  拷贝去向(如何拷贝数据)，--》  要将数据拷贝到 DishDto 中，要获取 DishDto 的对象
             *  拷贝完数据之后，就要将拷贝的数据添加到 DishDto 的集合中去
             */
/*

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            dishDtoList.add(dishDto);

            //要处理的 categoryName
            for (Category category : categoryList) {
                if (dish.getCategoryId().equals(category.getId())) {
                    //菜品 与 分类 匹配成功 ，就可以set了
                    dishDto.setCategoryName(category.getName());

                }
            }
        }
*/

        //方法2：jdk8流式运算
/*
        dishList.forEach(dish -> {
*/
/**
             *  拿到 dish 中的所有数据之后，就可以拷贝数据了：
             *  将 dishList 中的数据 拷贝到 dishDtoList 的集合中
             *  拷贝去向(如何拷贝数据)，--》  要将数据拷贝到 DishDto 中，要获取 DishDto 的对象
             *  拷贝完数据之后，就要将拷贝的数据添加到 DishDto 的集合中去
             */
/*

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            dishDtoList.add(dishDto);
            categoryList.stream().filter(
                    //过滤条件，分类的id与菜品的id匹配
                    category -> category.getId().equals(dish.getCategoryId()))
                    //只有菜品的id与分类的id匹配成功，才会进到 findAny 中执行查找
                        // findAny(),该方法是指在匹配成功的所有之中只会拿到一个
                        // ，findFirst(),:指在所有的匹配成功之中的id，只会拿第一个
                    .findAny()
                    .ifPresent(category -> {
                        //匹配成功，拿到之后，就会进入此方法中，就可以set了
                        dishDto.setCategoryName(category.getName());
                    });
        });
*/

        //方法3：map
        dishList.forEach(dish -> {
            /**
             *  拿到 dish 中的所有数据之后，就可以拷贝数据了：
             *  将 dishList 中的数据 拷贝到 dishDtoList 的集合中
             *  拷贝去向(如何拷贝数据)，--》  要将数据拷贝到 DishDto 中，要获取 DishDto 的对象
             *  拷贝完数据之后，就要将拷贝的数据添加到 DishDto 的集合中去
             */
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            dishDtoList.add(dishDto);

            //通过map来取，取到之后就可以set了
            Category category = categoryMap.getOrDefault(dish.getCategoryId(), new Category());
            dishDto.setCategoryName(category.getName());
            //dishDto.setCategoryName(String.valueOf(categoryMap.getOrDefault(dish.getCategoryId(),new Category())));
        });

        /**
         * 将数据填充到 records 集合中
         * List<T> records :就是代表的 DishDto
         * 将拷贝的数据 填充 到 DishDto 中
         */
        dishDtoPage.setRecords(dishDtoList);
        return dishDtoPage;
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * categoryName; //菜品分类名称
     * @param id
     * @return
     */
    @Override
    public DishDto getDishById(Long id) {
        //先从 Dish表中查询菜品基本信息
        //通过this调用，this 代表就是本方法
        //this.getById: 就是指 要使用此方法中的 getById去获取菜单的基本信息
        Dish dish = this.getById(id);

        //因为要使用到 DishDto 中的 菜品分类（DishDto）
        // ，而 DishDto 继承了 Dish
        // ，所以，就可以将查出的数据 拷贝到 DishDto中，同时也就可以用到 菜品分类（DishDto）
        // 这样，DishDto 中就有 封装了 Dish中所有的数据内容 ，其中数据也就与 菜品分类（categoryName）关联了
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        // 查询当前菜品对应的口味信息 ，从dish_flavor表中查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }

    /**
     * 修改数据
     * @param dishDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyById(DishDto dishDto) {
        // 修改菜品的其他字段
        this.updateById(dishDto);

        // 修改口味，dish里还有原有的口味
        // 原始数据： 河南烩面（辣度1，温度2）
        // 修改的数据： 河南烩面（辣度3，忌口4）
        //获取dishDto中的菜品口味信息
        List<DishFlavor> flavorList = dishDto.getFlavors();

        //遇到这种批量数据，或是多数据，要做统一修改的时候
        /**
         * 对菜品口味执行操作
         * 一般选择是全部删除，之后再试全部添加
         * 而前提是，要修改的这些数据，与其他的数据没有关系
         * 之后，就可进行条件删除，之后就可以再添加主句
         * 或者是先行查出在删除，之后再添加
         */
         LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
         //import：在执行条件删除的时候，一定要注意自己的条件，不能将整个表删除
        //将两个id匹配判断，如果相等，执行删除语句
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        this.dishFlavorService.remove(queryWrapper);

        /**
         * 添加菜品口味
         * 删除之后要新增
         * 新增时要设置菜品口味与菜品的关系
         * 关系就是：菜品口味中的id与菜品中的id是否关联
         */
        dishDto.getFlavors().forEach(dishFlavor -> {
            dishFlavor.setDishId(dishDto.getId());
        });
    }

    @Override
    public List<DishDto> getDishDtoList(Long categoryId, String name,Integer status) {
        // 查询构造器，查询时，id,name,状态，皆不可为空
        //先行查出菜品数据
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId!=null,Dish::getCategoryId,categoryId)
                .eq(status!=null,Dish::getStatus,status)
                .like(StringUtils.isNotBlank(name),Dish::getName,name)
                .orderByAsc(Dish::getSort);
        //用接口中的list方法，将数据存到集合中去
        List<Dish> list = this.list(queryWrapper);
        //因为DishDto 中有对应的菜品的口味信息，所以，Dish 要转成DishDto
        //创建一个DishDto的list集合，用来存DishDto数据
        List<DishDto> dishDtoList = new ArrayList<>();//核心
        /**
         * 将菜品口味信息塞进DishDto中
         * 通过菜品的id，查出所有的菜品所对应的口味信息
         *  ，为了避免拿到重复数据问题，转为toSet,用set集合接收，
         */
        Set<Long> dishId = list.stream().map(Dish::getId).collect(Collectors.toSet());

        //拿到所有对应的菜品信息之后，就可以得到所对应的口味信息
        List<DishFlavor> dishFlavorList = dishFlavorService.
                list(new LambdaQueryWrapper<DishFlavor>()
                        .in(DishFlavor::getDishId, dishId));

        //将拿到的所有数据，转为map
        Map<Long, List<DishFlavor>> flavorMap = dishFlavorList.stream().collect(Collectors.groupingBy(DishFlavor::getDishId));
        //遍历集合，处理数据
        list.forEach(dish -> {
            /**
             * 因为DishDto中有新增的字段-----》菜品口味信息,
             * 这是Dish中所没有的，且DishDto继承了Dish，所以要将数据拷贝到DishDto
             * 拷贝：先行创建接收拷贝数据的对象，拷贝到DishDto中，创建DishDto对象
             */
            DishDto dishDto = new DishDto();
            //拷贝数据到DishDto中
            BeanUtils.copyProperties(dish,dishDto);
            //将封装完成的数据添加到集合中
            dishDtoList.add(dishDto);
            //处理菜品口味信息    new ArraysList<>():相当于默认值
            dishDto.setFlavors(flavorMap.getOrDefault(dish.getId(),new ArrayList<>()));
        });
        return dishDtoList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteById(List<Long> ids) {
        //菜品删除
        /**
         * 在售中的菜品不可被删除
         * 通过 id查到即将被删除的数据，通过状态判断是否可以被删除
         */

        // 构造查询构造器
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        //通过id查到数据,查到数据之后，看一下当前菜品状态是否为在售状态：1，
        queryWrapper.in(Dish::getId,ids).eq(Dish::getStatus,1);
        //获去一共查到了多少的所有数据（获取有多少菜品为在售状态）
        int count = (int) this.count(queryWrapper);
        if (count >0) {
            //count >0 ，菜品在售中不可删除，直接抛出异常
            throw new BusinessException("菜品正在售卖中，不可被删除");
        }
        this.removeByIds(ids);
    }
}
