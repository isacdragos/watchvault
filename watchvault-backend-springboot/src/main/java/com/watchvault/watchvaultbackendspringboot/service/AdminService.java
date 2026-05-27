package com.watchvault.watchvaultbackendspringboot.service;

import com.watchvault.watchvaultbackendspringboot.dto.AdminUserResponse;
import com.watchvault.watchvaultbackendspringboot.entity.RoleEntity;
import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.error.BadRequestException;
import com.watchvault.watchvaultbackendspringboot.error.NotFoundException;
import com.watchvault.watchvaultbackendspringboot.repository.RoleRepository;
import com.watchvault.watchvaultbackendspringboot.repository.ShowRepository;
import com.watchvault.watchvaultbackendspringboot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShowRepository showRepository;
    private final AuthSessionService authSessionService;
    private final UserAccessService userAccessService;

    public AdminService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ShowRepository showRepository,
            AuthSessionService authSessionService,
            UserAccessService userAccessService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.showRepository = showRepository;
        this.authSessionService = authSessionService;
        this.userAccessService = userAccessService;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers(String requesterUsername) {
        UserEntity requester = userAccessService.requireAdmin(requesterUsername);

        return userRepository.findAll().stream()
                .map(user -> toAdminUserResponse(user, requester))
                .toList();
    }

    @Transactional
    public AdminUserResponse promoteUserToAdmin(String requesterUsername, Long targetUserId) {
        UserEntity requester = userAccessService.requireAdmin(requesterUsername);
        UserEntity target = findUser(targetUserId);

        if (!userAccessService.isAdmin(target)) {
            RoleEntity adminRole = roleRepository.findByNameIgnoreCase(UserAccessService.ADMIN_ROLE)
                    .orElseThrow(() -> new IllegalStateException("Missing ADMIN role."));
            target.getRoles().add(adminRole);
            target = userRepository.save(target);
        }

        return toAdminUserResponse(target, requester);
    }

    @Transactional
    public void deleteUser(String requesterUsername, Long targetUserId) {
        UserEntity requester = userAccessService.requireAdmin(requesterUsername);
        UserEntity target = findUser(targetUserId);

        if (requester.getId().equals(target.getId())) {
            throw new BadRequestException("Admins cannot delete themselves.");
        }

        if (userAccessService.isAdmin(target)) {
            throw new BadRequestException("Delete promoted admins manually from the database if needed.");
        }

        showRepository.deleteAllByUserId(target.getId());
        authSessionService.invalidateUserSessions(target.getId());
        target.getRoles().clear();
        userRepository.save(target);
        userRepository.delete(target);
    }

    private UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private AdminUserResponse toAdminUserResponse(UserEntity user, UserEntity requester) {
        boolean isAdmin = userAccessService.isAdmin(user);
        boolean isSelf = requester.getId().equals(user.getId());

        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                userAccessService.roleNames(user),
                userAccessService.permissionNames(user),
                !isAdmin,
                !isSelf && !isAdmin
        );
    }
}
