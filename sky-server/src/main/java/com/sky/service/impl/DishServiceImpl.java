package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService{
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional
    public void save(DishDTO dishDTO){
        //分两步，先插入一条菜品，然后再批量插入口味
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(flavor->{flavor.setDishId(dishId);});
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO){
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 删除菜品
     * @param ids
     */
    public void delete(List<Long> ids){
        //判断当前菜品能否删除,菜品是否在起售状态,status
      for(Long id:ids){
          Dish dish = dishMapper.getById(id);
          if(dish.getStatus()==StatusConstant.ENABLE)
          {
              throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
          }
      }
        //判断当前菜品是否关联套餐
        List<Long> setmealDishId = setmealDishMapper.getIdByDishId(ids);
        if(setmealDishId!=null&&setmealDishId.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        for(Long id:ids){
            //删除菜品和口味数据
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);
        }

  }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    public DishVO getById(Long id){
      Dish dish = dishMapper.getById(id);
      List<DishFlavor> flavor = dishFlavorMapper.getByDishId(id);
      DishVO dishVO = new DishVO();
      BeanUtils.copyProperties(dish,dishVO);
      dishVO.setFlavors(flavor);
      return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    public void update(DishDTO dishDTO){
        //修改菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //修改口味
        Long dishId = dish.getId();
        dishFlavorMapper.deleteByDishId(dishId);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(flavor->{flavor.setDishId(dishId);});
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
