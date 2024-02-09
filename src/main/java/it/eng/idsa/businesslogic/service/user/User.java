package it.eng.idsa.businesslogic.service.user;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class User implements UserDetails {

	private static final long serialVersionUID = -6994087319610173062L;

	String id;
	String username;
	String password;
	String role;

	User(final String id, final String username, final String password, String role) {
		super();
		this.id = requireNonNull(id);
		this.username = requireNonNull(username);
		this.password = requireNonNull(password);
		this.role = role;
	}

	User(final String id, final String username, final String password) {
		super();
		this.id = requireNonNull(id);
		this.username = requireNonNull(username);
		this.password = requireNonNull(password);
	}

	public String getId() {
		return id;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String toString() {
		return "User{" + "id='" + id + '\'' + ", username='" + username + '\'' + ", password='"
				+ (password != null && !password.isEmpty() ? "[PROTECTED]" : "[NOT SET]") + '\'' + ", role='" + role
				+ '\'' + ", accountNonExpired=" + isAccountNonExpired() + ", accountNonLocked=" + isAccountNonLocked()
				+ ", credentialsNonExpired=" + isCredentialsNonExpired() + ", enabled=" + isEnabled() + '}';
	}

}
