'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

/**
 * Function component for the Login Page.
 * Handles user login process and redirects based on user role.
 */
export default function LogIn() {
    // State variable for form data: email + password
    const [formData, setFormData] = useState({
      email: '',
      password: ''
    });
    // state variable for Errors
    const [errors, setErrors] = useState({});
    // state variable for showing password
    const [showPassword, setShowPassword] = useState(false);
    const router = useRouter();
  
    // Toggles the visibility of the password input field
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
  
    // Handles form submission for user login
    const handleSubmit = async (e) => {
      e.preventDefault();
  
      // Set up the URL and data for the login request
      const url = `https://${process.env.HOSTNAME}/api/auth/login`;
      const data = {
        email: formData.email,
        password: formData.password
      };
  
      try {
        // Making the fetch request to the login API
        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(data),
          credentials: 'include'  // Ensures cookies are sent and received with the request
        });
  
        // Check if the response is successful
        // if the response is unsuccessful, throw an error
        if (!response.ok) {
            setErrors({ general: `You don't have the right credentials!` });
        } 

        router.push('/');
      } catch (error) {
        console.error('Login error:', error);
        setErrors({ general: error.message });
      }
    };
  
    return (
        <div>
          <h2>Sign in to your account</h2>
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
                  autoComplete="current-password"
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
            {errors.general && <div>{errors.general}</div>}
            <div>
              <button type="submit">Sign in</button>
            </div>
          </form>
        </div>
      );
  }
  