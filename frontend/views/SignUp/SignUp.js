'use client';
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import authenticationService from '../../services/authenticationService';

/**
 * Function component for the Sign-up Page.
 * Handles user registration process and redirects to the login page.
 */
export default function SignUp() {
  // State variable for form data: email, password, and confirmPassword
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: ''
  });
  // State variable for errors
  const [errors, setErrors] = useState({});
  // State variable for showing password
  const [showPassword, setShowPassword] = useState(false);
  const router = useRouter();

  // Toggles the visibility of the password input fields
  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  // Handles input changes in the form fields
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  // Handles form submission for user registration
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validate password and confirmPassword match
    if (formData.password !== formData.confirmPassword) {
      setErrors({ general: 'Passwords do not match!' });
      return;
    }

    const data = {
      email: formData.email,
      password: formData.password
    };
  
    try {
      const response = await authenticationService.sendSignUpCredentials(data);

      // Check if the response is successful
      // If the response is unsuccessful, throw an error
      if (!response.ok) {
        const errorData = await response.json();
        setErrors({ general: errorData.message });
        return;
      }

      // Registration successful, redirect to the login page
      router.push('/log-in');
    } catch (error) {
      console.error('Sign-up error:', error);
      setErrors({ general: 'An error occurred during sign-up. Please try again.' });
    }
  };

  return (
    <div>
      <h2>Create an account</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="email-address">Email address</label>
          <input
            id="email-address"
            name="email"
            type="email"
            autoComplete="email"
            required
            placeholder="Email address"
            value={formData.email}
            onChange={handleChange}
          />
        </div>
        <div>
          <label htmlFor="password">Password</label>
          <div>
            <input
              id="password"
              name="password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              required
              placeholder="Password"
              value={formData.password}
              onChange={handleChange}
            />
            <button type="button" onClick={togglePasswordVisibility}>
              {showPassword ? 'Hide' : 'Show'}
            </button>
          </div>
        </div>
        <div>
          <label htmlFor="confirm-password">Confirm Password</label>
          <div>
            <input
              id="confirm-password"
              name="confirmPassword"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              required
              placeholder="Confirm Password"
              value={formData.confirmPassword}
              onChange={handleChange}
            />
          </div>
        </div>
        {errors.general && <div>{errors.general}</div>}
        <div>
          <button type="submit">Sign up</button>
        </div>
      </form>
      <p>
        Already have an account? <Link href="/log-in">Log in</Link>
      </p>
    </div>
  );
}
