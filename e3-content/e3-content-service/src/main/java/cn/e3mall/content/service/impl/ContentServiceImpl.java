package cn.e3mall.content.service.impl;

import java.util.Date;
import java.util.List;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.JsonUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.e3mall.common.utils.E3Result;
import cn.e3mall.content.service.ContentService;
import cn.e3mall.mapper.TbContentMapper;
import cn.e3mall.pojo.TbContent;
import cn.e3mall.pojo.TbContentExample;
import cn.e3mall.pojo.TbContentExample.Criteria;

/**
 * 内容管理Service
 * <p>Title: ContentServiceImpl</p>
 * <p>Description: </p>
 * <p>Company: www.itcast.cn</p>
 * @version 1.0
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Value("CONTENT_LIST")
	private String CONTENT_LIST;
	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private JedisClient jedisClient;

	@Override
	public EasyUIDataGridResult getItemList(Long categoryId, Integer page, Integer rows) {
		//设置分页信息
		PageHelper.startPage(page,rows);
		//创建查询条件
		TbContentExample example=new TbContentExample();
		TbContentExample.Criteria criteria = example.createCriteria();
		//设置查询条件
		criteria.andCategoryIdEqualTo(categoryId);
		//执行查询
		List<TbContent> contents = contentMapper.selectByExample(example);
		//取分页信息
		PageInfo<TbContent> pageInfo = new PageInfo(contents);
		//创建返回结果对象
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setTotal(pageInfo.getTotal());
		result.setRows(contents);
		return result;
	}
	/**
	 * 根据内容分类id查询内容列表
	 * <p>Title: getContentListByCid</p>
	 * <p>Description: </p>
	 * @param cid
	 * @return
	 * @see cn.e3mall.content.service.ContentService#getContentListByCid(long)
	 */
	@Override
	public List<TbContent> getContentListByCid(long cid) {
		try{
			//1.先查询缓存
			String json = jedisClient.hget(CONTENT_LIST, cid + "");
			if (StringUtils.isNotBlank(json)){
				List<TbContent> tbContents = JsonUtils.jsonToList(json, TbContent.class);
				return tbContents;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		//2.如果缓存中有直接相应结果
		//3.如果没有查询数据库
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		//设置查询条件
		criteria.andCategoryIdEqualTo(cid);
		//执行查询
		List<TbContent> list = contentMapper.selectByExampleWithBLOBs(example);
		try{
			//4.把结果添加到缓存
			jedisClient.hset(CONTENT_LIST,cid+"", JsonUtils.objectToJson(list));
		}catch (Exception e){
			e.printStackTrace();
		}

		return list;
	}
	@Override
	public E3Result addContent(TbContent content) {
		//补全些不能为空的字段信息
		content.setCreated(new Date());
		content.setUpdated(new Date());
		//执行插入语句
		contentMapper.insert(content);
		//缓存同步（添加的时候把缓存中的cid对应的值删除）
		jedisClient.hdel(CONTENT_LIST,content.getCategoryId().toString());
		return E3Result.ok();
	}

	@Override
	public E3Result editContent(TbContent content) {
		//设置修改时间
		content.setUpdated(new Date());
		//执行修改
		contentMapper.updateByPrimaryKeySelective(content);
		//缓存同步（添加的时候把缓存中的cid对应的值删除）
		jedisClient.hdel(CONTENT_LIST,content.getCategoryId().toString());
		return E3Result.ok();
	}

	@Override
	public TbContent selectByidContent(Long id) {
		//执行查询
		TbContent tbContent = contentMapper.selectByPrimaryKey(id);
		return tbContent;
	}

	@Override
	public E3Result deleteBatchContent(String[] ids) {
		//遍历集合
		for (String id  :ids) {
			//执行操作数据库
			contentMapper.deleteByPrimaryKey(Long.valueOf(id));
			//缓存同步（添加的时候把缓存中的cid对应的值删除）
			jedisClient.hdel(CONTENT_LIST,id.toString());
		}
		return E3Result.ok();
	}

}
