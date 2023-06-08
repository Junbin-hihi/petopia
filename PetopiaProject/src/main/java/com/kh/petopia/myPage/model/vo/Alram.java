package com.kh.petopia.myPage.model.vo;

import java.sql.Date;

import lombok.Data;

@Data
public class Alram {
	//b.category, b.create_date, b.board_title, r.nickname, r.reply_content
	
	// 전체 알림
	private String columnAll;
	private String dateAll;
	
	private String category;
	private String boardTitle;
	private String nickname;
	private String replyContent;
	
	// 공지사항
	private String qnaYn;
	private String couponName;
	
}