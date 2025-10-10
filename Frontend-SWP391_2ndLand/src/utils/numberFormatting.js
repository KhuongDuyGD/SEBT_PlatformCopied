// Utility functions for number formatting in CreateListing components
// Provides consistent number formatting across price, mileage, and other numeric fields

/**
 * Formats a number by adding dots as thousand separators
 * @param {string|number} value - The input value to format
 * @returns {string} - Formatted string with dots as separators
 */
export const formatNumberWithDots = (value) => {
  // Handle empty or null values
  if (!value && value !== 0) return '';
  
  // Convert to string and remove any existing non-digit characters except decimal point
  const cleanValue = String(value).replace(/[^\d.]/g, '');
  
  // Handle decimal numbers - split by decimal point
  const parts = cleanValue.split('.');
  const integerPart = parts[0];
  const decimalPart = parts[1];
  
  // Add dots as thousand separators to integer part
  const formattedInteger = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  
  // Return with decimal part if exists
  return decimalPart !== undefined ? `${formattedInteger}.${decimalPart}` : formattedInteger;
};

/**
 * Removes dots from formatted number to get the raw numeric value
 * @param {string} formattedValue - The formatted string with dots
 * @returns {string} - Clean numeric string
 */
export const parseFormattedNumber = (formattedValue) => {
  if (!formattedValue) return '';
  
  // Remove dots but keep decimal points intact
  return String(formattedValue).replace(/\.(?=.*\.)/g, '');
};

/**
 * Creates an input change handler that formats numbers with dots
 * @param {Function} originalOnChange - The original onChange function
 * @param {string} fieldName - Name of the field being handled
 * @returns {Function} - Enhanced onChange handler
 */
export const createFormattedInputHandler = (originalOnChange, fieldName) => {
  return (event) => {
    const { value } = event.target;
    
    // Parse the formatted value to get raw number
    const rawValue = parseFormattedNumber(value);
    
    // Create new event with raw value for form handling
    const newEvent = {
      ...event,
      target: {
        ...event.target,
        name: fieldName,
        value: rawValue
      }
    };
    
    // Call original onChange with raw value
    originalOnChange(newEvent);
  };
};

/**
 * Gets the display value for an input field (formatted with dots)
 * @param {string|number} rawValue - The raw numeric value from form state
 * @returns {string} - Formatted display value
 */
export const getDisplayValue = (rawValue) => {
  return formatNumberWithDots(rawValue);
};

/**
 * Format number to Vietnamese currency style without the trailing '₫' (UI will append if needed)
 * Uses Intl for consistency and fallbacks to local regex if Intl not available.
 * @param {number|undefined|null} value
 */
export const formatVnd = (value) => {
  if (value === null || value === undefined || isNaN(value)) return '';
  try {
    return new Intl.NumberFormat('vi-VN').format(value);
  } catch (_) {
    return formatNumberWithDots(value);
  }
};

/**
 * Format delta percent with sign and 1 decimal.
 * @param {number} delta
 */
export const formatDeltaPercent = (delta) => {
  if (delta === null || delta === undefined || isNaN(delta)) return '';
  const sign = delta > 0 ? '+' : (delta < 0 ? '' : '');
  return sign + delta.toFixed(1) + '%';
};

/**
 * Convert confidence (0..1) to percentage string (e.g., 0.934 -> 93%)
 */
export const formatConfidence = (c) => {
  if (c === null || c === undefined || isNaN(c)) return '';
  return Math.round(c * 100) + '%';
};

/**
 * Format clamp percent (0.12 -> ±12%)
 */
export const formatClampPercent = (p) => {
  if (p === null || p === undefined || isNaN(p)) return '';
  return '±' + (p * 100).toFixed(1) + '%';
};

/**
 * Format a multiplicative factor (e.g. 0.97 -> -3.0%, 1.15 -> +15.0%).
 */
export const formatFactorDelta = (f) => {
  if (f === null || f === undefined || isNaN(f)) return '';
  const delta = (f - 1) * 100;
  const sign = delta > 0 ? '+' : (delta < 0 ? '' : '');
  return sign + delta.toFixed(1) + '%';
};
