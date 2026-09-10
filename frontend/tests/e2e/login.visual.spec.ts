import {expect, test} from '@playwright/test';

test.describe('login visual regression', () => {
    test('renders the login screen consistently', async ({page}) => {
        await page.goto('/login');
        await expect(page).toHaveTitle(/Login|Operator Portal/);
        await expect(page.getByRole('heading', {name: /Bem-vindo de volta/i})).toBeVisible();
        await expect(page).toHaveScreenshot('login.png', {fullPage: true});
    });

    test('protects the root route for unauthenticated users', async ({page}) => {
        await page.goto('/');
        await expect(page).toHaveURL(/\/login$/);
    });
});
