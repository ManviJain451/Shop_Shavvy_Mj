package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private  final UserRepository userRepository;

    @Autowired
    public AdminService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Page<CustomerResponseDTO> getAllCustomers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<User> users;

        if (email != null && !email.isEmpty()) {
            users = userRepository.findByEmailContainingIgnoreCase(email, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(user -> new CustomerResponseDTO(
                user.getId(),
                user.getFirstName() + " " + (user.getMiddleName() != null ? user.getMiddleName() + " " : "") + user.getLastName(),
                user.getEmail(),
                user.getIsActive()
        ));
    }
}
