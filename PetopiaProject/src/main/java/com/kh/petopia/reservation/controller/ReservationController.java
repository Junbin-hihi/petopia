package com.kh.petopia.reservation.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.kh.petopia.admin.model.vo.Coupon;
import com.kh.petopia.member.model.vo.Member;
import com.kh.petopia.member.model.vo.Pet;
import com.kh.petopia.myPage.model.service.MyPageService;
import com.kh.petopia.reservation.model.service.ReservationService;
import com.kh.petopia.reservation.model.vo.Employee;
import com.kh.petopia.reservation.model.vo.Reservation;

@Controller
public class ReservationController {
	
	@Autowired
	private ReservationService reservationService;
	
	@Autowired
	private MyPageService myPageService;

	// 애견 미용 예약 step-1
	@RequestMapping("reservation.ps")
	public ModelAndView reservationPetSalon(int fee, ModelAndView mv, HttpSession session) {

		System.out.println(fee);
		
		// 1. 해당 미용실의 미용사 리스트를 조회해서 뿌려주기 
		// 참고로 미용실번호는 고정임 3번
		int psno = 3;
		
		ArrayList<Employee> eList = reservationService.selectEmployeeList(psno);
		System.out.println(eList);
		
		// 2. 로그인한 사용자의 마이펫 조회  myPageService
		Member loginMember = (Member)(session.getAttribute("loginMember"));
		Pet pet = myPageService.selectPet(loginMember.getMemberNo());
		System.out.println(pet);
		
		// 3. 미용 예약의 기본 금액 조회 
		
		mv.addObject("reservationFee",fee);
		mv.addObject("eList", eList);
		mv.addObject("pet", pet);
		
		mv.setViewName("reservation/reservationPetSalon");
		
		return mv;
	}
	
	// 사용자가 선택한 미용사의 예약 현황 조회 메소드
	@ResponseBody
	@RequestMapping(value="selectEmployeeReservation.ps", produces="application/json; charset=UTF-8")
	public String ajaxSelectEmployeeReservation(Reservation r, HttpSession session) {
		
		System.out.println("사용자가 선택한 미용사 번호 : " + r.getEmployeeNo());
		System.out.println("사용자가 입력한 예약 일자" + r.getCheckIn());
		
		
		System.out.println(reservationService.selectEmployeeReservation(r));
		
		
		return new Gson().toJson(reservationService.selectEmployeeReservation(r));
	}
	
	
	
	
	
	// 애견 미용 예약 step-2
	@RequestMapping("payment.ps")
	public ModelAndView paymentPetSalon(Reservation r, HttpSession session, ModelAndView mv) {
		
		Member loginMember = (Member)(session.getAttribute("loginMember"));
		int memberNo = loginMember.getMemberNo();
		
		r.setPetStoreNo(3);
		r.setMemberNo(memberNo);
		
		System.out.println(r);
		
		// 화면에 출력해야할 정보
		
		// 1. 사용자의 예약 날짜 및 시간 
		// Reservation 객체에 들어있음! 
		// r.getCheckIn(), r.getReservationTime()
		
		// 2. 사용자의 마이펫 정보 
		Pet pet = myPageService.selectPet(memberNo);
		
		// 3. 사용자가 선택한 담당 직원 
		// r.getEmployeeNo()로 select해오기 
		// employee VO로 아예 직원 정보를 불러올지 일단 고민 ... 
		
		// 4. 예약자 및 연락처 -> 로그인 정보 뿌려주면 됨 ! 
		
		
		// ----------------여기까지 해결 완 --------
		
		// 5. 사용자가 가지고있는 쿠폰이랑 적립금 조회
		
		// 5-1. 보유한 쿠폰 조회 
		int couponCount = myPageService.selectMemberCouponCount(memberNo);
		ArrayList<Coupon> cList = myPageService.selectMemberCouponList(memberNo);
		
		System.out.println(couponCount);
		System.out.println(cList);
		
		// 5-2. 적립금 조회 
		int point = myPageService.selectMemberPoint(memberNo);
		// System.out.println("적립금 출력 안 되냐?:"+point);
		
		// 보유 적립금 출력 
		
		
		
		// 6. 결제 정보 출력
		// 사용자의 입력에 따른 예약금액 
		// 사용자가 선택한 쿠폰과 적립금에 따른 총 결제 금액 
		
		
		mv.addObject("r",r);						// 사용자가 입력한 예약 정보
		mv.addObject("pet",pet);					// 사용자의 펫 정보
		mv.addObject("couponCount",couponCount);	// 쿠폰 개수
		mv.addObject("cList",cList);				// 사용자가 갖고있는 쿠폰 리스트 
		mv.addObject("point",point);				// 적립금 
		
		mv.setViewName("reservation/paymentPetSalon");
		
		return mv;
	}
	


	
	
}
