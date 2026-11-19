const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_apex_123";
const BOB_UID = "bob_apex_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, ts) {
  return {
    userId: uid,
    username: "murilo_apex",
    displayName: "Murilo",
    photoUrl: "",
    sex: "M",
    age: 26,
    weightKg: 78.5,
    heightCm: 178,
    waistCm: 82,
    chestCm: 102,
    armCm: 38,
    thighCm: 58,
    bodyFatPercent: 14.5,
    activityFactor: 1.55,
    goalDeltaKcal: 300,
    goalLabel: "Hipertrofia (Bulking)",
    onboardingCompleted: true,
    level: 1,
    currentXp: 0,
    nextLevelXp: 500,
    streakDays: 0,
    waterMl: 0,
    waterGoalMl: 2748,
    activeWorkoutName: "",
    activeWorkoutSubtitle: "",
    activeExerciseName: "",
    exerciseNotes: "",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validRoutinePayload(uid, ts) {
  return {
    userId: uid,
    letter: "A",
    statusBadge: "PRONTA",
    title: "Push A — Peito e Ombros",
    subtitle: "Alvo: 4x8-10 • RPE 8.5 • Descanso 90s",
    exerciseNames: ["Supino Reto com Barra", "Elevação Lateral Halteres"],
    targetSets: 4,
    targetReps: "8-10",
    targetRpe: 8.5,
    targetRestSeconds: 90,
    category: "hypertrophy",
    favorite: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validActiveSetPayload(uid, ts) {
  return {
    userId: uid,
    exerciseName: "Supino Reto com Barra",
    setOrder: 1,
    setLabel: "1",
    weightKg: 60,
    reps: 10,
    rpe: 8.0,
    completed: false,
    active: true,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validWorkoutLogPayload(uid, ts) {
  return {
    userId: uid,
    title: "Push A — Peito e Ombros",
    metaLine: "45 min • RPE 8.2 • 12 séries",
    tonnageKg: 6400,
    completedSets: 12,
    durationMinutes: 45,
    bestExercise: "Supino Reto com Barra",
    bestWeightKg: 80,
    bestReps: 8,
    iconType: "push",
    createdAt: ts,
  };
}

function validMealPayload(uid, ts) {
  return {
    userId: uid,
    timeLabel: "12:30",
    title: "Almoço Pós-Treino",
    description: "Arroz, patinho moído e brócolis",
    calories: 680,
    proteinG: 48,
    carbsG: 80,
    fatG: 16,
    fiberG: 9,
    completed: true,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validWeightLogPayload(uid, ts) {
  return {
    userId: uid,
    weightKg: 78.5,
    waistCm: 82,
    bodyFatPercent: 14.5,
    note: "Avaliação inicial da conta",
    createdAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. UNAUTHENTICATED ACCESS REJECTION
test("Unauthenticated user: denied across profile and all subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("routines").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("workout_logs").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("meals").get()
  );
});

// 2. USER PROFILE CREATE, PARTIAL UPDATES, AND SECURITY GUARDS
test("Owner: can create profile, update hydration/biometrics/progress, and is blocked from ghost or immutable edits", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const updated = new Date(Date.now() - 1000);

  // Create valid profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  // Partial update: hydration
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      waterMl: 500,
      updatedAt: updated,
    })
  );

  // Partial update: biometrics
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      weightKg: 79.2,
      waistCm: 81.5,
      waterGoalMl: 2772,
      updatedAt: updated,
    })
  );

  // Partial update: workout completion progress
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      level: 1,
      currentXp: 200,
      nextLevelXp: 500,
      streakDays: 1,
      activeWorkoutName: "",
      activeWorkoutSubtitle: "",
      activeExerciseName: "",
      updatedAt: updated,
    })
  );

  // Reject ghost field (Shadow Update Test)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: updated,
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      waterMl: 750,
      createdAt: updated,
      updatedAt: updated,
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      waterMl: 1000,
      updatedAt: new Date(Date.now() + 120000),
    })
  );

  // Reject invalid weight out of bounds
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      weightKg: 999,
      updatedAt: updated,
    })
  );

  // Reject list on /users collection (PII protection)
  await assertFails(aliceDb.collection("users").get());
});

// 3. CROSS-USER ISOLATION
test("Cross-user isolation: Bob cannot read or mutate Alice's profile or subcollections", async () => {
  const now = new Date(Date.now() - 5000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now));
    await db.collection("users").doc(ALICE_UID).collection("routines").doc("r1").set(validRoutinePayload(ALICE_UID, now));
    await db.collection("users").doc(ALICE_UID).collection("meals").doc("m1").set(validMealPayload(ALICE_UID, now));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ waterMl: 900, updatedAt: now }));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("routines").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("routines").doc("r1").delete());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("meals").get());
});

// 4. ROUTINES, ACTIVE SETS, WORKOUT LOGS, MEALS, AND WEIGHT LOGS PARITY
test("Owner: all Kotlin-mirrored subcollection creates, queries, updates, and deletes succeed; extra @DocumentId 'id' fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 8000);
  const updated = new Date(Date.now() - 1000);
  const userRef = aliceDb.collection("users").doc(ALICE_UID);

  // Routines: list empty subcollection succeeds
  await assertSucceeds(userRef.collection("routines").orderBy("createdAt", "desc").get());
  await assertSucceeds(userRef.collection("routines").doc("routine_1").set(validRoutinePayload(ALICE_UID, created)));
  await assertSucceeds(userRef.collection("routines").doc("routine_1").update({ favorite: true, updatedAt: updated }));
  // Reject @DocumentId 'id' inside routine payload
  await assertFails(
    userRef.collection("routines").doc("routine_bad").set({
      id: "routine_bad",
      ...validRoutinePayload(ALICE_UID, created),
    })
  );

  // Active sets: create, query, update, delete
  await assertSucceeds(userRef.collection("active_sets").orderBy("setOrder", "asc").get());
  await assertSucceeds(userRef.collection("active_sets").doc("set_1").set(validActiveSetPayload(ALICE_UID, created)));
  await assertSucceeds(
    userRef.collection("active_sets").doc("set_1").update({
      weightKg: 62.5,
      reps: 10,
      rpe: 8.5,
      completed: true,
      active: false,
      updatedAt: updated,
    })
  );
  await assertSucceeds(userRef.collection("active_sets").doc("set_1").delete());

  // Workout logs: create & query succeed; update forbidden (immutable history)
  await assertSucceeds(userRef.collection("workout_logs").orderBy("createdAt", "desc").get());
  await assertSucceeds(userRef.collection("workout_logs").doc("log_1").set(validWorkoutLogPayload(ALICE_UID, created)));
  await assertFails(userRef.collection("workout_logs").doc("log_1").update({ tonnageKg: 99999 }));

  // Meals: create, query, toggle completed, delete
  await assertSucceeds(userRef.collection("meals").orderBy("createdAt", "asc").get());
  await assertSucceeds(userRef.collection("meals").doc("meal_1").set(validMealPayload(ALICE_UID, created)));
  await assertSucceeds(userRef.collection("meals").doc("meal_1").update({ completed: false, updatedAt: updated }));
  await assertSucceeds(userRef.collection("meals").doc("meal_1").delete());

  // Weight logs: create & query succeed; update forbidden
  await assertSucceeds(userRef.collection("weight_logs").orderBy("createdAt", "desc").get());
  await assertSucceeds(userRef.collection("weight_logs").doc("w_1").set(validWeightLogPayload(ALICE_UID, created)));
  await assertFails(userRef.collection("weight_logs").doc("w_1").update({ weightKg: 70 }));
});
