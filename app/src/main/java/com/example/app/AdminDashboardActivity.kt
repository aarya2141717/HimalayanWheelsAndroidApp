package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.AppTheme

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AdminDashboardScreen()
            }
        }
    }
}

@Composable
fun AdminDashboardScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add Item */ },
                containerColor = Color(0xFF2F4CFA),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        },
        bottomBar = { AdminBottomNavBar() },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Top Bar
            item { AdminTopBar() }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Stats Grid
            item { StatsGrid() }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Needs Attention Section
            item {
                SectionHeader(title = "Needs Attention", actionText = "View All")
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { NeedsAttentionList() }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Recent Activity Section
            item {
                SectionHeader(title = "Recent Activity", actionText = "")
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { RecentActivityList() }
            
            // Bottom spacing for FAB
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun AdminTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.profile), // Make sure this exists or use a placeholder
            contentDescription = "Profile",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Admin Dashboard",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = "Himalayan Wheels",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                painter = painterResource(R.drawable.baseline_notifications_24),
                contentDescription = "Notifications",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun StatsGrid() {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            StatsCard(
                modifier = Modifier.weight(1f),
                title = "Total Vehicles",
                value = "142",
                percent = "+12%",
                iconRes = R.drawable.baseline_car_rental_24, // Replace with your car icon
                iconBgColor = Color(0xFFE3F2FD),
                iconTint = Color(0xFF1565C0),
                percentColor = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(16.dp))
            StatsCard(
                modifier = Modifier.weight(1f),
                title = "Total Bookings",
                value = "89",
                percent = "+5%",
                iconRes = R.drawable.baseline_calendar_month_24, // Replace with calendar icon
                iconBgColor = Color(0xFFF3E5F5),
                iconTint = Color(0xFF7B1FA2),
                percentColor = Color(0xFF4CAF50)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatsCard(
                modifier = Modifier.weight(1f),
                title = "Active Users",
                value = "1.2k",
                percent = "",
                iconRes = R.drawable.baseline_account_circle_24, // Replace with users icon
                iconBgColor = Color(0xFFFFF3E0),
                iconTint = Color(0xFFE65100),
                percentColor = Color.Transparent
            )
            Spacer(modifier = Modifier.width(16.dp))
            StatsCard(
                modifier = Modifier.weight(1f),
                title = "Requests",
                value = "5",
                percent = "5", // Using as a badge count
                isBadge = true,
                iconRes = R.drawable.baseline_visibility_24, // Replace with request/clipboard icon
                iconBgColor = Color(0xFFFFEBEE),
                iconTint = Color(0xFFC62828),
                percentColor = Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    percent: String,
    iconRes: Int,
    iconBgColor: Color,
    iconTint: Color,
    percentColor: Color,
    isBadge: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                if (isBadge) {
                     Box(
                        modifier = Modifier
                            .background(percentColor, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = percent,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (percent.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(percentColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = percent,
                            color = percentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, color = Color.Gray, fontSize = 12.sp)
            Text(text = value, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        if (actionText.isNotEmpty()) {
            TextButton(onClick = { /* TODO */ }) {
                Text(text = actionText, color = Color(0xFF2F4CFA))
            }
        }
    }
}

@Composable
fun NeedsAttentionList() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AttentionCard(
                imageRes = R.drawable.thar, // Replace with vehicle image
                name = "Jeep Wrangler",
                issue = "Engine Check",
                tag = "MAINT. DUE"
            )
        }
        item {
            AttentionCard(
                imageRes = R.drawable.royal_enfield, // Replace with vehicle image
                name = "Royal Enfield 350",
                issue = "Oil Change",
                tag = ""
            )
        }
    }
}

@Composable
fun AttentionCard(imageRes: Int, name: String, issue: String, tag: String) {
    Card(
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                if (tag.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFFFCDD2),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = tag,
                            color = Color(0xFFC62828),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_visibility_24), // Replace with wrench/tool icon
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = issue, color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RecentActivityList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.background(Color.White, RoundedCornerShape(24.dp)).padding(16.dp)
    ) {
        ActivityItem(
            title = "New User Registration",
            subtitle = "Rahul K. verified their license",
            time = "2m ago",
            iconRes = R.drawable.baseline_account_circle_24, // User icon
            iconBg = Color(0xFFE3F2FD),
            iconTint = Color(0xFF1565C0)
        )
        ActivityItem(
            title = "Booking Confirmed",
            subtitle = "Booking #892 for Mahindra Thar",
            time = "15m ago",
            iconRes = R.drawable.baseline_favorite_24, // Checkmark icon
            iconBg = Color(0xFFE8F5E9),
            iconTint = Color(0xFF2E7D32)
        )
        ActivityItem(
            title = "Vehicle Incident",
            subtitle = "Scratch reported on Himalayan 450",
            time = "1h ago",
            iconRes = R.drawable.baseline_visibility_off_24, // Alert icon
            iconBg = Color(0xFFFFEBEE),
            iconTint = Color(0xFFC62828)
        )
         ActivityItem(
            title = "New Vehicle Added",
            subtitle = "Added Scorpio N to fleet",
            time = "3h ago",
            iconRes = R.drawable.baseline_car_rental_24, // Car icon
            iconBg = Color(0xFFF3E5F5),
            iconTint = Color.Gray
        )
    }
}

@Composable
fun ActivityItem(title: String, subtitle: String, time: String, iconRes: Int, iconBg: Color, iconTint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Text(text = time, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun AdminBottomNavBar() {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_home_24), null) }, // Dashboard
            label = { Text("Dashboard") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2F4CFA), selectedTextColor = Color(0xFF2F4CFA))
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_car_rental_24), null) }, // Fleet
            label = { Text("Fleet") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_account_circle_24), null) }, // Users
            label = { Text("Users") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(painterResource(R.drawable.baseline_settings_24), null) }, // Settings (Using Lock as placeholder for settings if unavailable)
            label = { Text("Settings") }
        )
    }
}
