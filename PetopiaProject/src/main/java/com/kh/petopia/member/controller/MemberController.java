package com.kh.petopia.member.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.kh.petopia.common.model.vo.Attachment;
import com.kh.petopia.common.template.MyFileRename;
import com.kh.petopia.member.model.service.MemberService;
import com.kh.petopia.member.model.vo.Member;
import com.kh.petopia.member.model.vo.Pet;

@Controller
public class MemberController {
	

		
		@Autowired
		private MemberService memberService;
		
		@Autowired
		private BCryptPasswordEncoder bcyptPasswordEncoder;
		
		
		@GetMapping("login")
		public String loginView() {
			return "petopia/member/login";
		}
		
		/**
		 * 
		 * @param m : 사용자가 입력한 값으로 Member객체에 담겨 전달 받는다
		 * @param mv : ModelAndView
		 * @param session
		 * @return
		 */
		
		@RequestMapping("login.me")
		public ModelAndView login(Member m, ModelAndView mv, HttpSession session ) {
			
			Member loginMember = memberService.loginMember(m);
			
			if(loginMember != null && bcyptPasswordEncoder.matches(m.getMemberPwd(), loginMember.getMemberPwd())){
				session.setAttribute("loginMember", loginMember);
				mv.setViewName("redirect:/");
			}else {
				mv.addObject("alertMsg", "로그인에 실패했습니다. 다시 시도해주세요");
				mv.setViewName("common/errorPage");
			}
			return mv;
		}
		
		
		
		@RequestMapping("memberEnroll.me")
		public String memberEnroll() {
			return "petopia/member/memberEnrollForm";
		}
		
		
		@RequestMapping("join.me")
		public String joinMember(Member m, 
									MultipartFile upfile,
									String birthday_y,
									String birthday_m,
									String birthday_d,
									Pet pet,
									HttpSession session,
									Model model) {
			
			
			m.setMemberPwd(bcyptPasswordEncoder.encode(m.getMemberPwd()));
			m.setBirthday(birthday_y + birthday_m + birthday_d);
			
			int member = memberService.joinMember(m);
			
			Attachment memberAtt = insertMemberFile(upfile, session);
			
			int att = memberAtt != null ? memberService.joinMember(memberAtt): 0;
			int memberPet = pet.getIsPet().equals("Y")? memberService.joinMember(pet): 0;
			
			System.out.println(m);
			System.out.println(pet);
			
			if(member + att + memberPet > 0) {
				model.addAttribute("alertMsg", "회원가입이 원료되었습니다");
				return "redirect:/";
			}else {
				model.addAttribute("errorMsg", "회원가입이 실패되었습니다");
				return "common/errorPage";
			}
		
		}
		
		
		public Attachment insertMemberFile(MultipartFile upfile, HttpSession session) {
			
			if(!upfile.getOriginalFilename().equals("")) {
				Attachment memberAtt = new Attachment();
				
				memberAtt.setOriginName(upfile.getOriginalFilename());
				memberAtt.setChangeName(MyFileRename.saveFile(session, upfile));
				memberAtt.setFilePath("resources/uploadfiles/");
				memberAtt.setFileCategory(4);
				return memberAtt;
			}
			return null;
		}
		
		@GetMapping("findEmail.me")
		public String findEmailView() {
			return "petopia/member/findInfo";
		}
		

		
	
		
	
	

}
