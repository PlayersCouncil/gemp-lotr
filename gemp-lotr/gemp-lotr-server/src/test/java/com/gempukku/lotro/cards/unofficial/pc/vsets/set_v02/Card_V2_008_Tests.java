package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "102_8");

					put("swordsman", "54_78");  // Lorien Swordsman: valiant Elf companion, strength 5, vitality 3
					put("guardian", "4_76");    // Lorien Guardian: Elf companion who is not valiant, strength 5
					put("guard", "5_83");       // Household Guard: valiant [rohan] Man, strength 6; Valiant is its whole text
					put("guard2", "5_83");
					put("dwarf", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, no text in play

					put("bow", "1_41");         // Elven Bow: [elven] ranged weapon; bearer is an archer
					put("bow2", "1_41");
					put("bow3", "1_41");
					// Naith Longbow: [elven] ranged weapon for an Elf companion, strength +1; it does not make its
					// bearer an archer, and its archery ability is never used
					put("longbow", "6_21");
					put("sword", "4_64");       // Elven Sword: [elven] hand weapon, strength +1
					put("cloak", "1_42");       // Elven Cloak: [elven] possession that is no weapon
					put("lembas", "4_75");      // Lembas: [elven] possession; Regroup: discard it to reconcile your hand
					put("aiglos", "9_12");      // Aiglos: [elven] artifact, a hand weapon; bearer must be an Elf
					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon; bearer must be a Dwarf
					put("slits", "5_80");       // Arrow-slits: Maneuver: spot 2 [rohan] Men to discard a possession

					put("smith", "3_60");       // Isengard Smith: Regroup: exert to discard a weapon (or all Free
					put("smith2", "3_60");      //   Peoples possessions if you can spot 5 companions); vitality 3
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGoldenHallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "102_8");
					put("swordsman", "54_78");  // Lorien Swordsman: valiant Elf companion
					put("guardian", "4_76");    // Lorien Guardian: Elf companion
					put("dwarf", "1_7");        // Dwarf Guard: Dwarf companion, no text in play
					put("bow", "1_41");         // Elven Bow: [elven] possession, a ranged weapon
					put("sword", "4_64");       // Elven Sword: [elven] possession, a hand weapon
					put("aiglos", "9_12");      // Aiglos: [elven] artifact, a hand weapon; bearer must be an Elf
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, a hand weapon; bearer must be a Dwarf
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					// Golden Hall: when the fellowship moves here, all Free Peoples weapons must be discarded.
					put("site3", "4_338");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaldirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Haldir, Naith Commander
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Valiant.
		 * While Haldir bears a ranged weapon, each valiant Elf is strength +1 and each valiant Elf does not
		 * add to the fellowship archery total.
		 * Each time an [elven] possession is about to be discarded by a card effect, you may exert Haldir to
		 * prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haldir");

		assertEquals("Haldir", card.getBlueprint().getTitle());
		assertEquals("Naith Commander", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedAndWithNoRangedWeaponGivesNoValiantElfAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		scn.MoveCardsToHand(haldir);
		scn.MoveCompanionsToTable(swordsman);

		scn.StartGame();

		assertEquals(5, scn.GetStrength(swordsman));
		assertTrue(scn.FreepsPlayAvailable(haldir));

		scn.FreepsPlayCard(haldir);
		assertInZone(Zone.FREE_CHARACTERS, haldir);
		assertEquals(6, scn.GetStrength(haldir));
		assertEquals(5, scn.GetStrength(swordsman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingARangedWeaponOnHaldirMakesEachValiantElfStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var guardian = scn.GetFreepsCard("guardian");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCardsToHand(bow);
		scn.MoveCompanionsToTable(haldir, swordsman, guardian, guard);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(haldir));
		assertEquals(5, scn.GetStrength(swordsman));
		assertEquals(5, scn.GetStrength(guardian));
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsPlayCard(bow);
		scn.FreepsChooseCard(haldir);
		assertAttachedTo(bow, haldir);

		// Valiant Elves: Haldir himself and another companion.  The Elven Bow adds no strength.
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(swordsman));
		// An Elf who is not valiant, and a valiant Man.
		assertEquals(5, scn.GetStrength(guardian));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRangedWeaponMustBeHaldirsOwnAndNeedNotMakeHimAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var longbow = scn.GetFreepsCard("longbow");
		scn.MoveCompanionsToTable(haldir, swordsman);
		// Haldir bears a hand weapon; another valiant Elf bears the ranged weapon.
		scn.AttachCardsTo(haldir, sword);
		scn.AttachCardsTo(swordsman, bow);
		scn.MoveCardsToHand(longbow);

		scn.StartGame();

		// 6 + 1 for the Elven Sword, and nothing else.
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(5, scn.GetStrength(swordsman));

		// A Naith Longbow: a ranged weapon that is no Elven Bow and makes nobody an archer.  The Lorien Swordsman
		// has a ranged weapon already, so Haldir is the only Elf companion who can take it.
		scn.FreepsPlayCard(longbow);
		assertAttachedTo(longbow, haldir);
		assertFalse(scn.HasKeyword(haldir, Keyword.ARCHER));

		// 6 + 1 for the Elven Sword + 1 for the Naith Longbow + 1.
		assertEquals(9, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(swordsman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ValiantElvesStopAddingToTheFellowshipArcheryTotalWhenHaldirTakesUpARangedWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var guardian = scn.GetFreepsCard("guardian");
		var guard = scn.GetFreepsCard("guard");
		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var bow3 = scn.GetFreepsCard("bow3");
		scn.MoveCardsToHand(bow3);
		scn.MoveCompanionsToTable(haldir, swordsman, guardian, guard);
		// An Elven Bow makes its bearer an archer.
		scn.AttachCardsTo(swordsman, bow);
		scn.AttachCardsTo(guardian, bow2);
		// Haldir bears a weapon already, and not a ranged one.
		scn.AttachCardsTo(haldir, scn.GetFreepsCard("sword"));

		scn.StartGame();
		// A valiant archer who is no Elf.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, guard, null, Keyword.ARCHER));
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.FreepsRefreshDecision();

		// Three archers: the valiant Elf, the Elf who is not valiant and the valiant Man.
		assertEquals(3, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(bow3);
		assertAttachedTo(bow3, haldir);
		assertTrue(scn.HasKeyword(haldir, Keyword.ARCHER));

		// Four archers now, and the two valiant Elves among them, Haldir included, add nothing: the Elf who is
		// not valiant and the valiant Man go on adding.
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ExertsEachTimeAnElvenPossessionIsAboutToBeDiscardedUntilHaldirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(haldir, swordsman);
		// A ranged weapon on Haldir and a hand weapon on another companion.
		scn.AttachCardsTo(haldir, bow);
		scn.AttachCardsTo(swordsman, sword);

		var smith = scn.GetShadowCard("smith");
		var smith2 = scn.GetShadowCard("smith2");

		scn.StartGame();
		scn.MoveMinionsToTable(smith, smith2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(haldir));
		scn.ShadowUseCardAction(smith);
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, sword));
		scn.ShadowChooseCard(sword);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsChooseOptionalTrigger(haldir);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertAttachedTo(sword, swordsman);
		assertEquals(1, scn.GetWoundsOn(smith));

		// A second weapon, Haldir's own.
		scn.FreepsPass();
		scn.ShadowUseCardAction(smith2);
		scn.ShadowChooseCard(bow);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsChooseOptionalTrigger(haldir);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertAttachedTo(bow, haldir);

		// The first weapon again.  Vitality 3 with 2 wounds: Haldir cannot exert, and nothing is offered.
		scn.FreepsPass();
		scn.ShadowUseCardAction(smith);
		scn.ShadowChooseCard(sword);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(haldir));
		assertInDiscard(sword);
		assertAttachedTo(bow, haldir);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnElvenArtifactOrADwarvenPossessionAndMayBeDeclinedForAnElvenPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var guardian = scn.GetFreepsCard("guardian");
		var dwarf = scn.GetFreepsCard("dwarf");
		var aiglos = scn.GetFreepsCard("aiglos");
		var axe = scn.GetFreepsCard("axe");
		var sword = scn.GetFreepsCard("sword");
		// Frodo and three more: the Smith does not spot 5 companions, and discards one weapon at a time.
		scn.MoveCompanionsToTable(haldir, guardian, dwarf);
		// Three hand weapons, each on a bearer of its own: an [elven] artifact on an Elf, a [dwarven] possession
		// on a Dwarf, and an [elven] possession on Haldir himself.
		scn.AttachCardsTo(guardian, aiglos);
		scn.AttachCardsTo(dwarf, axe);
		scn.AttachCardsTo(haldir, sword);

		var smith = scn.GetShadowCard("smith");
		var smith2 = scn.GetShadowCard("smith2");

		scn.StartGame();
		scn.MoveMinionsToTable(smith, smith2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		scn.ShadowUseCardAction(smith);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aiglos, axe, sword));
		scn.ShadowChooseCard(aiglos);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(haldir));
		assertInDiscard(aiglos);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPass();
		scn.ShadowUseCardAction(smith2);
		scn.ShadowChooseCard(axe);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(haldir));
		assertInDiscard(axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The Elven Sword is the only weapon left, so the Smith's third use needs no choice.
		scn.FreepsPass();
		scn.ShadowUseCardAction(smith);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(sword);
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenEveryPossessionIsAboutToBeDiscardedEachElvenOneMayBeSavedForAnExertionAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var guardian = scn.GetFreepsCard("guardian");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		var cloak = scn.GetFreepsCard("cloak");
		var axe = scn.GetFreepsCard("axe");
		var aiglos = scn.GetFreepsCard("aiglos");
		var dwarf = scn.GetFreepsCard("dwarf");
		// Frodo and four more: the Smith spots 5 companions.
		scn.MoveCompanionsToTable(haldir, swordsman, guardian, dwarf);
		scn.AttachCardsTo(haldir, bow);
		scn.AttachCardsTo(swordsman, sword, cloak);
		// The Dwarven Axe is on a Dwarf, and the Elf beside him bears the [elven] artifact.
		scn.AttachCardsTo(dwarf, axe);
		scn.AttachCardsTo(guardian, aiglos);

		var smith = scn.GetShadowCard("smith");

		scn.StartGame();
		scn.MoveMinionsToTable(smith);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		scn.ShadowUseCardAction(smith);

		// Four possessions are about to be discarded.  Haldir may save an [elven] one, and not the Dwarven Axe.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsChooseOptionalTrigger(haldir);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, sword, cloak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe, aiglos));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(cloak);

		// And another, for another exertion.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsChooseOptionalTrigger(haldir);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(cloak, axe));
		scn.FreepsChooseCard(sword);

		// Exhausted, he is offered nothing for the third.
		assertAttachedTo(cloak, swordsman);
		assertAttachedTo(sword, swordsman);
		assertInDiscard(bow, axe);
		// Aiglos is an artifact, and was never among the possessions the Smith discards.
		assertAttachedTo(aiglos, guardian);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenASiteDiscardsEveryWeaponOnlyAnElvenPossessionAmongThemMayBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGoldenHallScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var guardian = scn.GetFreepsCard("guardian");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		var aiglos = scn.GetFreepsCard("aiglos");
		var axe = scn.GetFreepsCard("axe");
		var dwarf = scn.GetFreepsCard("dwarf");
		scn.MoveCompanionsToTable(haldir, swordsman, guardian, dwarf);
		scn.AttachCardsTo(haldir, bow);
		scn.AttachCardsTo(swordsman, sword);
		// The [elven] artifact on an Elf and the [dwarven] possession on a Dwarf.
		scn.AttachCardsTo(guardian, aiglos);
		scn.AttachCardsTo(dwarf, axe);

		scn.StartGame();
		scn.SkipToSite(2);
		// The fellowship moves to Golden Hall, and four weapons are about to be discarded together.
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsChooseOptionalTrigger(haldir);
		assertEquals(1, scn.GetWoundsOn(haldir));

		// The two [elven] possessions may be saved; the [elven] artifact and the [dwarven] possession may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aiglos, axe));
		scn.FreepsChooseCard(sword);

		// Offered the other, he lets it go.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsDeclineOptionalTrigger();

		assertAttachedTo(sword, swordsman);
		assertInDiscard(bow, aiglos, axe);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADiscardByAFreePeoplesCardIsPreventedTheSameWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		var slits = scn.GetFreepsCard("slits");
		// Two [rohan] Men for Arrow-slits to spot.
		scn.MoveCompanionsToTable(haldir, swordsman, scn.GetFreepsCard("guard"), scn.GetFreepsCard("guard2"));
		scn.AttachCardsTo(haldir, bow);
		scn.AttachCardsTo(swordsman, sword);
		scn.MoveCardsToSupportArea(slits);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(slits));
		scn.FreepsUseCardAction(slits);
		assertTrue(scn.FreepsHasCardChoiceAvailable(bow, sword));
		scn.FreepsChooseCard(sword);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsChooseOptionalTrigger(haldir);

		assertEquals(1, scn.GetWoundsOn(haldir));
		assertAttachedTo(sword, swordsman);
		assertAttachedTo(bow, haldir);
		// Arrow-slits discards itself as well, and is a condition.
		assertInDiscard(slits);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ADiscardPaidAsACostIsPreventedTooAndTheAbilityItWasToPayForDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var lembas = scn.GetFreepsCard("lembas");
		scn.MoveCompanionsToTable(haldir, swordsman);
		scn.AttachCardsTo(swordsman, lembas);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.GetFreepsDeckCount() >= 8);
		assertTrue(scn.FreepsActionAvailable(lembas));
		scn.FreepsUseCardAction(lembas);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsChooseOptionalTrigger(haldir);

		// Lembas was not discarded, so its cost was not paid and the hand is not reconciled.
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertAttachedTo(lembas, swordsman);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The same ability again, and this time Haldir lets it go.
		scn.ShadowPass();
		scn.FreepsUseCardAction(lembas);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(haldir));
		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(lembas);
		assertEquals(1, scn.GetWoundsOn(haldir));
		// Reconciled: an empty hand is drawn up to 8.
		assertEquals(8, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAnElvenPossessionIsDiscardedBecauseItsBearerIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var swordsman = scn.GetFreepsCard("swordsman");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(haldir, swordsman);
		scn.AttachCardsTo(swordsman, bow);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();

		// 13 against 5: the Lorien Swordsman is overwhelmed.
		assertEquals(5, scn.GetStrength(swordsman));
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsAssignAndResolve(swordsman, troop);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, swordsman);
		assertInDiscard(bow);
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(haldir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
