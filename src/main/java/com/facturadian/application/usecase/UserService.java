package com.facturadian.application.usecase;

import com.facturadian.domain.exception.BusinessException;
import com.facturadian.domain.model.AppUser;
import com.facturadian.application.port.in.UserUseCase;
import com.facturadian.application.port.out.PasswordHasherPort;
import com.facturadian.application.port.out.UserRepositoryPort;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService implements UserUseCase {

    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_]{4,20}$");
    private static final Pattern FULL_NAME = Pattern.compile("^[\\p{L} .'-]{3,120}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepositoryPort repository;
    private final PasswordHasherPort hasher;
    /** Hash ficticio: se compara cuando el usuario no existe para que el tiempo de respuesta sea parejo. */
    private final String dummyHash;

    public UserService(UserRepositoryPort repository, PasswordHasherPort hasher) {
        this.repository = repository;
        this.hasher = hasher;
        this.dummyHash = hasher.hash("contrasena-ficticia-que-nunca-se-usa");
    }

    @Override
    public AppUser register(String username, String fullName, String email, String password, String confirmPassword) {
        String user = username == null ? "" : username.trim().toLowerCase();
        String name = fullName == null ? "" : fullName.trim();
        String mail = email == null ? "" : email.trim().toLowerCase();

        if (!USERNAME.matcher(user).matches()) {
            throw new BusinessException("El usuario debe tener entre 4 y 20 caracteres: letras, números o guion bajo.");
        }
        if (!FULL_NAME.matcher(name).matches()) {
            throw new BusinessException("Escribe tu nombre completo (solo letras, mínimo 3 caracteres).");
        }
        if (mail.length() > 120 || !EMAIL.matcher(mail).matches()) {
            throw new BusinessException("El correo electrónico no es válido.");
        }
        validatePassword(password, confirmPassword, user);

        if (repository.findByUsername(user).isPresent()) {
            throw new BusinessException("Ese nombre de usuario ya está en uso.");
        }
        if (repository.findByEmail(mail).isPresent()) {
            throw new BusinessException("Ya existe una cuenta con ese correo.");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(user);
        appUser.setFullName(name);
        appUser.setEmail(mail);
        appUser.setPasswordHash(hasher.hash(password));
        return repository.save(appUser);
    }

    @Override
    public Optional<AppUser> authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty() || password.length() > 128) {
            hasher.matches("x", dummyHash);
            return Optional.empty();
        }
        Optional<AppUser> found = repository.findByUsername(username.trim().toLowerCase());
        if (found.isEmpty()) {
            hasher.matches(password, dummyHash);
            return Optional.empty();
        }
        AppUser user = found.get();
        if (user.isLocked()) {
            hasher.matches(password, dummyHash);
            return Optional.empty();
        }
        if (!hasher.matches(password, user.getPasswordHash())) {
            user.registerFailedAttempt();
            repository.save(user);
            return Optional.empty();
        }
        user.registerSuccessfulLogin();
        repository.save(user);
        return Optional.of(user);
    }

    @Override
    public Optional<AppUser> findActiveUser(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return repository.findByUsername(username.trim().toLowerCase()).filter(u -> !u.isLocked());
    }

    private void validatePassword(String password, String confirm, String username) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException("La contraseña es obligatoria.");
        }
        if (password.length() < 10 || password.length() > 72) {
            throw new BusinessException("La contraseña debe tener entre 10 y 72 caracteres.");
        }
        boolean upper = false, lower = false, digit = false, symbol = false;
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) upper = true;
            else if (Character.isLowerCase(ch)) lower = true;
            else if (Character.isDigit(ch)) digit = true;
            else symbol = true;
        }
        if (!(upper && lower && digit && symbol)) {
            throw new BusinessException("La contraseña debe incluir mayúscula, minúscula, número y un símbolo.");
        }
        if (password.toLowerCase().contains(username)) {
            throw new BusinessException("La contraseña no puede contener tu nombre de usuario.");
        }
        if (!password.equals(confirm)) {
            throw new BusinessException("Las contraseñas no coinciden.");
        }
    }
}