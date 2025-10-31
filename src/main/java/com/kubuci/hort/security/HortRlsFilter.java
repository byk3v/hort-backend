// package com.kubuci.hort.security;
//
// import java.io.IOException;
// import java.sql.Connection;
// import java.sql.SQLException;
//
// import javax.sql.DataSource;
//
// import org.springframework.jdbc.datasource.DataSourceUtils;
// import org.springframework.security.core.context.SecurityContextHolder;
// import
// org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;
//
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
//
// @Component
// public class HortRlsFilter extends OncePerRequestFilter {
// private final DataSource ds;
//
// public HortRlsFilter(DataSource ds) {
// this.ds = ds;
// }
//
// @Override
// protected void doFilterInternal(HttpServletRequest request,
// HttpServletResponse response, FilterChain filterChain)
// throws ServletException, IOException {
// var auth = SecurityContextHolder.getContext()
// .getAuthentication();
// String hortId = null;
// if (auth instanceof JwtAuthenticationToken jwt) {
// Object claim = jwt.getToken()
// .getClaim("hort_id");
// hortId = claim != null
// ? claim.toString()
// : null;
// }
// Connection c = null;
// try {
// if (hortId != null && !hortId.isBlank()) {
// c = DataSourceUtils.getConnection(ds);
// try (
// var ps = c.prepareStatement("select set_config('app.hort_id', ?, false)")) {
// ps.setString(1, hortId);
// ps.execute();
// }
// }
// filterChain.doFilter(request, response);
// }
// catch (SQLException e) {
// throw new ServletException(e);
// }
// finally {
// if (c != null) {
// try (
// var ps = c.prepareStatement("select set_config('app.hort_id', null, false)"))
// {
// ps.execute();
// }
// catch (SQLException ignored) {
// }
// DataSourceUtils.releaseConnection(c, ds);
// }
// }
// }
// }
