package cn.e3mall.service.impl;

import java.util.Date;
import java.util.List;

import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemDescExample;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.pojo.TbItemExample.Criteria;
import cn.e3mall.service.ItemService;

/**
 * 商品管理Service
 * <p>Title: ItemServiceImpl</p>
 * <p>Description: </p>
 * <p>Company: www.itcast.cn</p> 
 * @version 1.0
 */
@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemDescMapper itemDescMapper;
	
	@Override
	public TbItem getItemById(long itemId) {
		//根据主键查询
		//TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		//设置查询条件
		criteria.andIdEqualTo(itemId);
		//执行查询
		List<TbItem> list = itemMapper.selectByExample(example);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public EasyUIDataGridResult getItemList(int page, int rows) {
		//设置分页信息
		PageHelper.startPage(page, rows);
		//执行查询
		TbItemExample example = new TbItemExample();
		List<TbItem> list = itemMapper.selectByExample(example);
		//创建一个返回值对象
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setRows(list);
		//取分页结果
		PageInfo<TbItem> pageInfo = new PageInfo<>(list);
		//取总记录数
		long total = pageInfo.getTotal();
		result.setTotal(total);
		return result;
	}

	@Override
	public E3Result addItem(TbItem item, String desc) {
		//1.生成商品id
		long itemId = IDUtils.genItemId();
		//2.补全item的属性
		item.setId(itemId);
		item.setStatus((byte)1);
		item.setCreated(new Date());
		item.setUpdated(new Date());
		//3.向商品表插入数据
		itemMapper.insert(item);
		//4.创建一个商品描述表对应的pojo对象
		TbItemDesc itemDesc = new TbItemDesc();
		//5.补全属性
		itemDesc.setItemDesc(desc);
		itemDesc.setItemId(itemId);
		itemDesc.setCreated(new Date());
		itemDesc.setUpdated(new Date());
		//6.向商品描述表插入数据
		itemDescMapper.insert(itemDesc);
		//7.返回成功
		return E3Result.ok();
	}

	@Override
	public TbItemDesc selectTbItemDesc(long id) {
		TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(id);
		return itemDesc;
	}

	@Override
	public E3Result update(TbItem item, String desc) {
		// 1、根据商品id，更新商品表，条件更新
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andIdEqualTo(item.getId());
		itemMapper.updateByExampleSelective(item,example);
		// 2、根据商品id，更新商品描述表，条件更新
		TbItemDesc tbItemDesc = new TbItemDesc();
		tbItemDesc.setItemDesc(desc);
		TbItemDescExample descExample = new TbItemDescExample();
		TbItemDescExample.Criteria descCriteria = descExample.createCriteria();
		descCriteria.andItemIdEqualTo(item.getId());
		itemDescMapper.updateByExampleSelective(tbItemDesc,descExample);
		return E3Result.ok();
	}

	@Override
	public E3Result deleteBatch(String ids) {
		//判断ids不为空
		if(StringUtils.isNoneBlank(ids)){
			//分割ids
			String[] split = ids.split(",");
			for ( String id : split ) {
				itemMapper.deleteByPrimaryKey(Long.valueOf(id));
				itemDescMapper.deleteByPrimaryKey(Long.valueOf(id));
			}
			return E3Result.ok();
		}
		return null;
	}

	@Override
	public E3Result updateByShelves(String ids) {
		if (StringUtils.isNotBlank(ids)){
			String[] split = ids.split(",");
			for (String id : split) {
				TbItem item = itemMapper.selectByPrimaryKey(Long.valueOf(id));
				item.setStatus((byte)2);
				itemMapper.updateByPrimaryKey(item);
				System.out.println("updateToStatus 2:"+id);
			}
			return E3Result.ok();
		}
		return null;
	}

	@Override
	public E3Result updateByinstock(String ids) {
		if (StringUtils.isNotBlank(ids)){
			String[] split = ids.split(",");
			for (String id : split) {
				TbItem item = itemMapper.selectByPrimaryKey(Long.valueOf(id));
				item.setStatus((byte)1);
				itemMapper.updateByPrimaryKey(item);
				System.out.println("updateToStatus 1:"+id);
			}
			return E3Result.ok();
		}
		return null;
	}

}
