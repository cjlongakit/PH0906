# React Practice Activities

This repository contains three React practice exercises to help you learn components, state, and hooks.

## Practice Files

Each practice is available as a standalone HTML file that you can open directly in your browser:

### Individual Practices

1. **practice1-press-button.html** - Create a Clickable Button
   - Learn how to use onClick event handlers
   - Display alert messages on user interaction

2. **practice2-click-counter.html** - Build a Click Counter
   - Use the useState hook to track state
   - Implement conditional styling based on state
   - Show conditional content

3. **practice3-change-text.html** - Change Text on Click
   - Update text dynamically with useState
   - Handle multiple button interactions
   - Apply conditional styling

### Combined Demo

- **react-practices-all.html** - All three practices in one page
  - View all exercises together
  - Compare different React patterns

## How to Use

### Option 1: Open Locally
Simply double-click any HTML file to open it in your default browser. All React libraries are loaded from CDN, so no installation is required.

### Option 2: Use an Online Editor
You can copy the code from these files to online editors:

#### CodePen Setup
1. Go to [CodePen](https://codepen.io/)
2. Create a new pen
3. Click Settings → JavaScript
4. Set JavaScript Preprocessor to "Babel"
5. Add external scripts:
   - React: `https://unpkg.com/react@17/umd/react.development.js`
   - ReactDOM: `https://unpkg.com/react-dom@17/umd/react-dom.development.js`
6. Copy the HTML code (the `<div id="root"></div>` part) to the HTML section
7. Copy the JavaScript code (the part inside `<script type="text/babel">`) to the JS section
8. Copy any CSS from the `<style>` section to the CSS section

## What You'll Learn

### Practice 1: Event Handlers
- How to attach event listeners to React elements
- Using arrow functions in event handlers
- Triggering browser APIs (like alert) from React

### Practice 2: State Management
- How to use the useState hook
- Updating state based on user interactions
- Conditional rendering with logical operators
- Dynamic styling based on state

### Practice 3: Multiple State Updates
- Managing state with different values
- Handling multiple buttons with different actions
- Applying conditional CSS properties
- Creating interactive text displays

## Check Your Understanding

Each practice includes questions to help you verify your understanding:

### Practice 1
- What React feature makes the button "listen" for a click?
- Where does the message come from?

### Practice 2
- How does useState help React remember the number of clicks?
- Which part of the code updates the count?

### Practice 3
- What triggers the text change?
- How is this similar to how a mobile app updates its display?

## Technical Details

- **React Version**: 17.x
- **ReactDOM Version**: 17.x
- **Babel**: Standalone (for JSX transformation)
- **No build process required** - everything runs in the browser

## Troubleshooting

If the examples don't work:

1. **Check your browser console** for any error messages
2. **Ensure you have an internet connection** (required to load React from CDN)
3. **Make sure JavaScript is enabled** in your browser
4. **Try a different browser** if issues persist (Chrome, Firefox, or Edge recommended)

## Next Steps

After completing these practices:

1. Try modifying the code to add new features
2. Combine concepts from multiple practices
3. Experiment with different event handlers (onMouseEnter, onDoubleClick, etc.)
4. Add more state variables to track different values
5. Create your own custom React components

## License

These practice files are provided for educational purposes.
