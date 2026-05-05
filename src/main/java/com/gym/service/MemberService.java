// ============= MemberService.java =============
package com.gym.service;

import com.gym.dao.MemberDAO;
import com.gym.entity.Member;
import java.util.List;
import java.util.Optional;

public class MemberService {
    private final MemberDAO memberDAO;
    
    public MemberService() {
        this.memberDAO = new MemberDAO();
    }
    
    public void createMember(Member member) {
        if (member.getMemberCode() == null || member.getMemberCode().isEmpty()) {
            member.setMemberCode(memberDAO.generateMemberCode());
        }
        memberDAO.save(member);
    }
    
    public void updateMember(Member member) {
        memberDAO.update(member);
    }
    
    public void deleteMember(Long id) {
        memberDAO.delete(id);
    }
    
    public Optional<Member> getMemberById(Long id) {
        return memberDAO.findById(id);
    }
    
    public Optional<Member> getMemberByCode(String code) {
        return memberDAO.findByMemberCode(code);
    }
    
    public Optional<Member> getMemberByPhone(String phone) {
        return memberDAO.findByPhone(phone);
    }
    
    public List<Member> getAllMembers() {
        return memberDAO.findAll();
    }
    
    public List<Member> searchMembers(String keyword) {
        return memberDAO.searchMembers(keyword);
    }
    
    public List<Member> getMembersByStatus(Member.MemberStatus status) {
        return memberDAO.findByStatus(status);
    }
    
    public long getTotalMembers() {
        return memberDAO.count();
    }
    
    public long getActiveMembersCount() {
        return memberDAO.findByStatus(Member.MemberStatus.ACTIVE).size();
    }
}