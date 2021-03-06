package com.callke8.system.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.common.CommonController;
import com.callke8.system.org.Org;
import com.callke8.system.scheduleassign.SysScheduleAssign;
import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 时间调度计划实时
 * 
 * @author hasee
 *
 */
public class SysSchedule extends Model<SysSchedule>{
	
	private static final long serialVersionUID = 1L;
	
	public static final SysSchedule dao = new SysSchedule();
	
	/**
	 * 添加调度计划
	 * 
	 * @param s
	 * @return
	 */
	public boolean add(SysSchedule s) {
		
		boolean b = false;
		
		if(s.save()) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 删除调度计划，通过scheduleId 删除
	 * 
	 * @param scheduleId
	 * @return
	 */
	public boolean deleteByScheduleId(String scheduleId) {
		
		boolean b = false;
		int count = 0;
		
		count = Db.update("delete from sys_schedule where SCHEDULE_ID=?",scheduleId);
		
		if(count > 0) {
			b = true;
			//删除成功后，还需要强制的，将这个调度任务的分配情况删除
			SysScheduleAssign.dao.deleteSysScheduleAssignBySchedule_Id(scheduleId);
		}
		
		return b;
	}
	
	/**
	 * 删除调度计划
	 * 
	 * @param s
	 * @return
	 */
	public boolean delete(SysSchedule s) {
		if(BlankUtils.isBlank(s)) {
			return false;
		}
		
		String scheduleId = s.get("SCHEDULE_ID");
		
		return deleteByScheduleId(scheduleId);
		
	}
	
	
	/**
	 * 修改调度计划
	 * 
	 * @param s
	 * 		调度计划实体
	 * @return
	 */
	public boolean update(SysSchedule s) {
		
		boolean b = false;
		
		String sql = "update sys_schedule set SCHEDULE_NAME=?,DATETYPE=?,DATETYPE_DETAIL=?";
		
		sql += ",STARTHOUR1=?,STARTMINUTE1=?,ENDHOUR1=?,ENDMINUTE1=?";
		sql += ",STARTHOUR2=?,STARTMINUTE2=?,ENDHOUR2=?,ENDMINUTE2=?";
		sql += ",STARTHOUR3=?,STARTMINUTE3=?,ENDHOUR3=?,ENDMINUTE3=?";
		sql += ",STARTHOUR4=?,STARTMINUTE4=?,ENDHOUR4=?,ENDMINUTE4=?";
		sql += ",STARTHOUR5=?,STARTMINUTE5=?,ENDHOUR5=?,ENDMINUTE5=?";
		
		sql += ",MAXTIMEITEM=?";
		
		sql += " where SCHEDULE_ID=?";
		
		int count = Db.update(sql,s.get("SCHEDULE_NAME"),s.getInt("DATETYPE"),s.get("DATETYPE_DETAIL"),s.get("STARTHOUR1"),s.get("STARTMINUTE1"),s.get("ENDHOUR1"),s.get("ENDMINUTE1"),s.get("STARTHOUR2"),s.get("STARTMINUTE2"),s.get("ENDHOUR2"),s.get("ENDMINUTE2"),s.get("STARTHOUR3"),s.get("STARTMINUTE3"),s.get("ENDHOUR3"),s.get("ENDMINUTE3"),s.get("STARTHOUR4"),s.get("STARTMINUTE4"),s.get("ENDHOUR4"),s.get("ENDMINUTE4"),s.get("STARTHOUR5"),s.get("STARTMINUTE5"),s.get("ENDHOUR5"),s.get("ENDMINUTE5"),s.getInt("MAXTIMEITEM"),s.get("SCHEDULE_ID"));
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	/**
	 * 按页查询
	 * 
	 * @param currentPage
	 * 				当前页码
	 * @param numPerPage
	 * 				每页显示数量
	 * @param orgCode
	 * 				组织编码
	 * @return
	 */
	public Page<Record> getScheduleByPaginate(int pageNumber,int pageSize,String scheduleName,String dateType,String orgCode) {
		
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[5];          //参数的对象集
		int index = 0;
		
		sb.append("from sys_schedule where 1=1");
		
		if(!BlankUtils.isBlank(scheduleName)) {
			sb.append(" and SCHEDULE_NAME like ?");
			pars[index] = "%" + scheduleName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(dateType) && !dateType.equalsIgnoreCase("empty")) {
			sb.append(" and DATETYPE=?");
			pars[index] = dateType;
			index++;
		}
		
		if(!BlankUtils.isBlank(orgCode)) {
			sb.append(" and ORG_CODE=?");
			pars[index] = orgCode;
			index++;
		}
		
		Page<Record> p = Db.paginate(pageNumber, pageSize, "select *", sb.toString() + " ORDER BY SCHEDULE_ID DESC", ArrayUtils.copyArray(index, pars));
		
		return p;
		
	}
	
	public Map getScheduleByPaginateToMap(int pageNumber,int pageSize,String scheduleName,String dateType,String orgCode) {
		
		/**
		 * 先查询出当前 page 的数量
		 */
		Page<Record> p = getScheduleByPaginate(pageNumber, pageSize,scheduleName,dateType,orgCode);
		int total = p.getTotalRow();
		
		List<Record> newList = new ArrayList<Record>();   //创建新的list，用于将每条记录所属的部门的名字设置进去
		
		for(Record r:p.getList()) {
			
			String oc = r.get("ORG_CODE");   //得到组织编码
			
			Record o = Org.dao.getOrgByOrgCode(oc);  //取出组织（部门）
			if(!BlankUtils.isBlank(o)) {
				r.set("ORG_CODE_DESC", o.get("ORG_NAME"));
			}
			
			//设置日期类型详细情况
			String dateTypeDetailDesc = "";
			if(r.getInt("DATETYPE")==1) {
				dateTypeDetailDesc = "每天";
			}else if(r.getInt("DATETYPE")==2) {    //按星期
				
				dateTypeDetailDesc = "星期:(";
				
				String dateTypeDetail = r.getStr("DATETYPE_DETAIL");   //得到日期类型的详细，主要是以数字分隔的数字，用于表示星期几
				String[] dtds = dateTypeDetail.split(",");
			
				for(String str:dtds) {
					//根据数据字典组码和字典项码，取出字典项的Record
					Record dictItemRecord = CommonController.getDictItemFormMemoryVariable("WEEK", str);
					if(!BlankUtils.isBlank(dictItemRecord)) {
						dateTypeDetailDesc += "&nbsp;&nbsp;" + dictItemRecord.getStr("DICT_NAME");
					}
				}
				
				dateTypeDetailDesc += ")";
			}
			r.set("DATETYPE_DETAIL_DESC", dateTypeDetailDesc);
			
			//System.out.println("查询出来的实体情况是：" + r);
			newList.add(r);
		}
		
		Map map = new HashMap();
		map.put("total", total);
		map.put("rows", newList);
		
		return map;
	}
	
	/**
	 * 根据当前登录用户分配到的调度任务返回
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @param scheduleName
	 * @param dateType
	 * @param orgCode
	 * @param currOperId
	 * @return
	 */
	public Map getScheduleByPaginateToMapByScheduleAssign(int pageNumber,int pageSize,String scheduleName,String dateType,String orgCode,String currOperId) {
		
		//查询出当前用户被分配到调度任务情况
		List<Record> assignList = SysScheduleAssign.dao.getSysScheduleAssignByOperId(currOperId);   //取出当前操作员被分配到的记录情况
		if(BlankUtils.isBlank(assignList) || assignList.size()==0) {    //如果当前操作员没有被分配到任何的记录，则无需再往下查询了。直接返回空的记录
			Map map = new HashMap();
			map.put("total", 0);
			map.put("rows", new ArrayList<Record>());
			return map;
		}
		
		/**
		 * 先查询出当前 page 的数量
		 */
		Page<Record> p = getScheduleByPaginate(pageNumber, pageSize,scheduleName,dateType,orgCode);
		int total = p.getTotalRow();
		
		List<Record> newList = new ArrayList<Record>();   //创建新的list，用于将每条记录所属的部门的名字设置进去
		
		
		for(Record r:p.getList()) {
			
			String scheduleId = r.getStr("SCHEDULE_ID");    //取得当前记录中调度任务的ID
			
			//检查当前的记录是否被分配给了当前的用户
			boolean beAssigned = false;
			for(Record alr:assignList) {
				String assignScheduleIdRs = alr.getStr("SCHEDULE_ID");
				if(scheduleId.equals(assignScheduleIdRs)) {    //只要两者相同，则表示已经被分配
					beAssigned = true;
				}
			}
			
			if(!beAssigned) {    //如果没有被分配，则跳过此记录
				continue;
			}
			
			String oc = r.get("ORG_CODE");   //得到组织编码
			
			Record o = Org.dao.getOrgByOrgCode(oc);  //取出组织（部门）
			if(!BlankUtils.isBlank(o)) {
				r.set("ORG_CODE_DESC", o.get("ORG_NAME"));
			}
			
			//设置日期类型详细情况
			String dateTypeDetailDesc = "";
			if(r.getInt("DATETYPE")==1) {
				dateTypeDetailDesc = "每天";
			}else if(r.getInt("DATETYPE")==2) {    //按星期
				
				dateTypeDetailDesc = "星期:(";
				
				String dateTypeDetail = r.getStr("DATETYPE_DETAIL");   //得到日期类型的详细，主要是以数字分隔的数字，用于表示星期几
				String[] dtds = dateTypeDetail.split(",");
				
				for(String str:dtds) {
					//根据数据字典组码和字典项码，取出字典项的Record
					Record dictItemRecord = CommonController.getDictItemFormMemoryVariable("WEEK", str);
					if(!BlankUtils.isBlank(dictItemRecord)) {
						dateTypeDetailDesc += "&nbsp;&nbsp;" + dictItemRecord.getStr("DICT_NAME");
					}
				}
				
				dateTypeDetailDesc += ")";
			}
			r.set("DATETYPE_DETAIL_DESC", dateTypeDetailDesc);
			
			//System.out.println("查询出来的实体情况是：" + r);
			newList.add(r);
		}
		
		Map map = new HashMap();
		map.put("total", newList.size());
		map.put("rows", newList);
		
		return map;
	}

	
	public List<Record> getAllSchedules() {
		
		List<Record> list = null;
		
		String sql = "select * from sys_schedule";
		
		list = Db.find(sql);
		
		return list;
	}
	
	/**
	 * 根据调度计划ID，取出调度计划信息
	 * 
	 * @param scheduleId
	 * @return
	 */
	public SysSchedule getScheduleById(String scheduleId) {
		
		if(BlankUtils.isBlank(scheduleId)) {
			return null;
		}
		
		String sql = "select * from sys_schedule where SCHEDULE_ID=?";
		
		SysSchedule schedule = findFirst(sql, scheduleId);
		
		return schedule;
	}
	
	
	/**
	 * 
	 * 根据调度任务ID,判断当前日期及时间是否处于调度期内
	 * 
	 * @param scheduleId
	 * @return
	 */
	public boolean checkScheduleIsActive(String scheduleId) {
		
		boolean b = false;
		
		//如果调度ID为空时
		if(BlankUtils.isBlank(scheduleId)) {
			return b;
		}
		
		//先根据调度任务ID,取出调度任务
		SysSchedule schedule = getScheduleById(scheduleId);
		if(BlankUtils.isBlank(schedule)) {
			return b;
		}
		
		
		//（1）第一步判断当前日期的星期数是否存在于设置的星期数中
		//取出星期的日期分布,格式：1,2,3,6,7
		String dateTypeDetail = schedule.get("DATETYPE_DETAIL");
		int dateType = schedule.getInt("DATETYPE");       //日期类型：1表示 每天； 2表示按星期几的方式
		if(dateType==1) {                 //如果日期类型为每天时，则 dateTypeDetail 强制设置为 1,2,3,4,5,6,7
			dateTypeDetail = "1,2,3,4,5,6,7";
		}
		
		//得到当天是星期几
		int dayOfWeek = DateFormatUtils.getDayOfWeek(DateFormatUtils.getFormatDate());
		
		if(!StringUtil.containsAny(dateTypeDetail, String.valueOf(dayOfWeek))) {   //查看今天的星期几是否存在于设置的日期中
			b = false;
			return b;
		}
		
		//（2）第二步判断当前的时间是否存在于
		//格式：11、01、23
		String currHour = DateFormatUtils.formatDateTime(new Date(),"HH");   //当前时间小时数
		//格式：00、05、22、59
		String currMinute = DateFormatUtils.formatDateTime(new Date(),"mm"); //当前时间分钟数
		
		//组成一个数字：
		int currTime = Integer.valueOf((currHour + currMinute));
		
		int maxTimeItem = schedule.getInt("MAXTIMEITEM");   //取得总的时间项数量
		
		for(int i=1;i <= maxTimeItem; i++) {
			
			String startHour = schedule.get("STARTHOUR" + i);    
			String startMinute = schedule.get("STARTMINUTE" + i);
			String endHour = schedule.get("ENDHOUR" + i);
			String endMinute = schedule.get("ENDMINUTE" + i);
			
			int startTime = Integer.valueOf((startHour + startMinute));
			int endTime = Integer.valueOf((endHour + endMinute));
			
			//如果当前的时间大于开始时间，且当前时间小于结束时间
			if(currTime >= startTime && currTime <= endTime) {   //只要有一个时间项微信符合条件,即可返回正确
				b = true;
				return b;
			}
			
		}
		
		return b;
	}

}
