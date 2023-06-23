package com.kh.petopia.member.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.kh.petopia.common.model.vo.Attachment;
import com.kh.petopia.common.template.MyFileRename;
import com.kh.petopia.member.model.service.KakaoService;
import com.kh.petopia.member.model.service.MemberService;
import com.kh.petopia.member.model.vo.Member;
import com.kh.petopia.member.model.vo.Pet;
import com.kh.petopia.myPage.model.service.MyPageService;

@Controller
public class MemberController {
	
	
		
		@Autowired
		private MemberService memberService;
		
		@Autowired
		private BCryptPasswordEncoder bcyptPasswordEncoder;
		
		
		@Autowired
		private MyPageService myPageService;
	
		@Autowired
		private KakaoService kakaoService;
		
		@GetMapping("login")
		public String loginView() {
			return "member/login";
		}
		
		
		
		//사용자가 입력한 값으로 Member객체에 담겨 전달 받는다
		
		@RequestMapping("login.member")
		public ModelAndView login(Member m, ModelAndView mv, HttpSession session ) {
			Member loginMember = memberService.loginMember(m.getEmail());
			
			if(loginMember != null && bcyptPasswordEncoder.matches(m.getMemberPwd(), loginMember.getMemberPwd())){
				loginMember.setRating(myPageService.getMemberRating(loginMember.getMemberNo()));
				session.setAttribute("loginMember", loginMember);
				mv.setViewName("redirect:/");
			}else {
				mv.addObject("errorMsg", "로그인에 실패했습니다. 다시 시도해주세요");
				mv.setViewName("common/errorPage");
			}
			return mv;
		}
		
		@RequestMapping("logout.member" )
		public String logoutMember(HttpSession session) {
			session.invalidate();
			return "redirect:/";
		}
		
		
		
		@RequestMapping("memberEnroll.member")
		public String memberEnroll() {
			return "member/memberEnrollForm";
		}
		
		
		@RequestMapping("join.member")
		public ModelAndView joinMember(Member m, 
									MultipartFile upfile,
									String birthday_y,
									String birthday_m,
									String birthday_d,
									Pet pet,
									HttpSession session,
									ModelAndView model) {
			
			
			m.setMemberPwd(bcyptPasswordEncoder.encode(m.getMemberPwd()));
			//System.out.println(m);
			m.setBirthday(birthday_y + birthday_m + birthday_d);
			
			int member = memberService.joinMember(m);
			//System.out.println(member);
			
			Attachment memberAtt = insertMemberFile(upfile, session);
			System.out.println(memberAtt);
			
			int att = memberAtt != null ? memberService.joinMember(memberAtt): 0;
			int memberPet = pet.getIsPet().equals("Y")? memberService.joinMember(pet): 0;
			
			
//			System.out.println(m);
//			System.out.println(pet);
			
			if(member + att + memberPet > 0) {
				model.addObject("alertMsg", "회원가입이 원료되었습니다")
				.setViewName("member/login");
			}else {
				model.addObject("errorMsg", "회원가입이 실패되었습니다")
				.setViewName("common/errorPage");
				
			}
		
			return model;
		}
		
		
		public Attachment insertMemberFile(MultipartFile upfile, HttpSession session) {
			
			if(!upfile.getOriginalFilename().equals("")) {
				Attachment memberAtt = new Attachment();
				
				memberAtt.setOriginName(upfile.getOriginalFilename());
				memberAtt.setChangeName(MyFileRename.saveFile(session, upfile));
				memberAtt.setFilePath("resources/uploadFiles/");
				memberAtt.setFileCategory(4);
				return memberAtt;
			}
			return null;
		}
		
		
		//이메일 찾기 요청시 화면 변경 값 전달하는 메소드
		@GetMapping("findEmail")
		public ModelAndView findEmailView(ModelAndView mv) {
			mv.addObject("title", "이메일 찾기")
			.addObject("findTitle","가입한 닉네임 ")
			.setViewName("member/findInfo");
			
			return mv;
		
		}

		
		@GetMapping("findPwd")
		public ModelAndView findPwdView(ModelAndView mv) {
			
			mv.addObject("title", "비밀번호 찾기")
			.addObject("findTitle","가입한 이메일 ")
			.setViewName("member/findInfo");
			
			return mv;
			
		}
		
		/**
		 * 비밀번호 변경 화면으로 전환하는 메소드
		 * @return
		 */
		@GetMapping("resetPwd")
		public String resetPwd() {
			return "member/resetPwd";
		}
		
	
		//회원 정보 수정 화면 
		@GetMapping("updateInfo.me")
		public ModelAndView updateInfo(HttpSession session,
										ModelAndView mv) {
			
			int memberNo = ((Member)session.getAttribute("loginMember")).getMemberNo();
			mv.addObject("memberAtt", myPageService.selectMemberImage(memberNo))
			.setViewName("member/memberUpdateForm");
			return mv;
		}
		
		//회원 정보 수정
		@PostMapping("updateInfo.me")
		public String updateInfo(Member m,
								 MultipartFile upfile,
								 String memberAtt,
								 HttpSession session) {


			int updateMember = memberService.updateMember(m);
			System.out.println(memberAtt);
			
			if(!upfile.getOriginalFilename().equals("")) {
				Attachment attachment = insertMemberFile(upfile, session);
				attachment.setRefNo(m.getMemberNo());
				//기존파일 있으면
				if(memberAtt != null) {
					new File(session.getServletContext().getRealPath(memberAtt.substring(22))).delete();
					updateMember = memberService.updateMember(attachment);
				}else {
					//기존파일이 없으면
					updateMember = memberService.joinMember(attachment);
				}
			}
			System.out.println(updateMember);
			if(updateMember >0) session.setAttribute("alertMsg", "회원 정보가 수정되었습니다");
			else session.setAttribute("alertMsg", "회원 정보 수정 실패") ;
			
			return "redirect:updateInfo.me";
			
			
//			if(att != null) {
//				if(!memberAtt.equals("")) {
//					new File(session.getServletContext().getRealPath(memberAtt.substring(22))).delete();
//					att.setRefNo(m.getMemberNo());
//					updateMember = memberService.updateMember(m, att);
//				}
//							
//			}
				
			
		}
		
		
		//카카오로그인
		@GetMapping("kakaoMemberEnroll.member")
		
		public ModelAndView kakaoLogin(@RequestParam String code, 
										HttpSession session,
										ModelAndView mv) throws IOException, ParseException {
			//http://localhost:8282/petopia/memberEnroll.member?code=UeauyYwUPDS68VBMWof45HaDZUCIFhpm0-sew0RHmSuz4aUczLGM_WdVb6b_775lcm-Q2worDNMAAAGI5fVyGA
			//System.out.println(code);
			
			String accessToken = kakaoService.getToken(code);
			String email = kakaoService.getUserInfo(accessToken);
			if(!email.equals("") && memberService.emailCheck(email)>0 ) {
				 session.setAttribute("loginMember", memberService.loginMember(email));
				 mv.setViewName("redirect:/");
			}else {
				 mv.addObject("email", email)
				 .setViewName("member/memberEnrollForm");
			}
			return mv;
		}
		
		
		
		
		
}
