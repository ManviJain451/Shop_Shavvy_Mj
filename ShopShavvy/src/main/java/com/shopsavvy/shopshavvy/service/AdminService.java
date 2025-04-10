package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.model.users.Seller;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private  final UserRepository userRepository;

    @Autowired
    public AdminService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<CustomerResponseDTO> getAllCustomers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<User> customers;

        if (email != null && !email.isEmpty()) {
            customers = userRepository.findByEmailContainingIgnoreCaseAndRoles(email, "ROLE_CUSTOMER", pageable);
        } else {
            customers = userRepository.findByRoles("ROLE_CUSTOMER", pageable);
        }

        return customers.stream()
                .map(user -> new CustomerResponseDTO(
                        user.getId(),
                        user.getFirstName() + " " + (user.getMiddleName() != null ? user.getMiddleName() + " " : "") + user.getLastName(),
                        user.getEmail(),
                        user.getIsActive()
                ))
                .collect(Collectors.toList());
    }

    public List<SellerResponseDTO> getAllSellers(int pageSize, int pageOffset, String sort, String email) {
        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));
        Page<User> sellers;

        if (email != null && !email.isEmpty()) {
            sellers = userRepository.findByEmailContainingIgnoreCaseAndRoles(email, "ROLE_SELLER", pageable);
        } else {
            sellers = userRepository.findByRoles("ROLE_SELLER", pageable);
        }

        return sellers.stream()
                .map(user -> {
                    Seller seller = (Seller) user;
                    return new SellerResponseDTO(
                            seller.getId(),
                            seller.getFirstName() + " " + (seller.getMiddleName() != null ? seller.getMiddleName() + " " : "") + seller.getLastName(),
                            seller.getEmail(),
                            seller.getIsActive(),
                            seller.getCompanyName(),
                            seller.getAdresses(),
                            seller.getCompanyContact()
                    );
                })
                .collect(Collectors.toList());
    }
}
