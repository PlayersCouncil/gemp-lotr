package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "15_87");
					put("poleaxe", "11_95");    // Poleaxe: a [men] hand weapon; its Skirmish ability is used only on purpose
					put("greataxe", "15_80");   // Great Axe: a [men] hand weapon; its bonus needs a hunter companion
					put("bow", "11_89");        // Long Battle Bow: a [men] ranged weapon, no hand weapon; cheated on, so its play trigger is not set off
					put("haradrim", "11_80");   // Ferocious Haradrim: a minion of strength 12; his text answers a Free Peoples kill
					put("hillman", "15_82");    // Grousing Hillman: another [men] Man, strength 9; his text needs a controlled site
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3
					put("sword", "1_112");      // Ranger's Sword: strength +2 for Aragorn, played from hand
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "15_87");
					put("dagger", "18_69");     // Henchman's Dagger: a hand weapon a [men] minion bears, strength +1
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PrimitiveSavageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Primitive Savage
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While you can spot a companion of strength 12 or more, this minion is strength +5.<br>Each time this minion wins a skirmish, if he bears a hand weapon, you may take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("savage");

		assertEquals("Primitive Savage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledWinningASkirmishWithAHandWeaponOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var savage = scn.GetShadowCard("savage");
		var dagger = scn.GetShadowCard("dagger");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, dagger);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 13 + 1 for the Dagger against 8: Aragorn loses, and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(savage));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledWinningASkirmishWithAHandWeaponOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var savage = scn.GetShadowCard("savage");
		var dagger = scn.GetShadowCard("dagger");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, dagger);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// The Savage has won, bearing a hand weapon.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void IsStrengthPlus5WhileACompanionOfStrength12CanBeSpottedAndNotForAnAllyOrAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var haradrim = scn.GetShadowCard("haradrim");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		// Laid early: minions in play later in the turn (Q-0228).
		scn.MoveMinionsToTable(savage, haradrim);
		// Strengths that only arrange the board: Aragorn 8 + 2, Frodo 3 + 1 for the Ring + 7, the ally 2 + 10.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 7));
		scn.ApplyAdHocModifier(new StrengthModifier(null, maggot, null, 10));
		scn.FreepsRefreshDecision();

		// A companion of 11, an ally of 12 and another minion of 12: none of them counts.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(11, scn.GetStrength(frodo));
		assertEquals(12, scn.GetStrength(maggot));
		assertEquals(12, scn.GetStrength(haradrim));
		assertEquals(13, scn.GetStrength(savage));
		assertInZone(Zone.HAND, sword);

		scn.FreepsPlayCard(sword);

		// Aragorn at exactly 12.
		assertAttachedTo(sword, aragorn);
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(13 + 5, scn.GetStrength(savage));
		// The other minion is not made stronger.
		assertEquals(12, scn.GetStrength(haradrim));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRingBearerAtStrength12MakesHimStrengthPlus5Too() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// 3 + 1 for the Ring + 7
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 7));

		assertEquals(11, scn.GetStrength(frodo));
		assertEquals(13, scn.GetStrength(savage));

		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 1));

		assertEquals(12, scn.GetStrength(frodo));
		assertEquals(13 + 5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LosingASkirmishBearingAHandWeaponOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 could be taken.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, poleaxe);
		assertAttachedTo(poleaxe, savage);
		// Aragorn 8 + 14 = 22 beats the Savage's 13 + 2 + 5 (Aragorn is of strength 12 or more).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 14));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertAttachedTo(poleaxe, savage);
		assertEquals(22, scn.GetStrength(aragorn));
		assertEquals(20, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertFalse(scn.IsSiteControlled(site1));

		scn.PassCurrentPhaseActions();

		// He lost, and lives with a wound.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void WinningBearingARangedWeaponOrAnotherMinionWinningWithAHandWeaponOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var bow = scn.GetShadowCard("bow");
		var hillman = scn.GetShadowCard("hillman");
		var greataxe = scn.GetShadowCard("greataxe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveMinionsToTable(savage, hillman);
		scn.AttachCardsTo(hillman, greataxe);
		assertAttachedTo(greataxe, hillman);

		scn.SkipToAssignments();
		// The Bow makes its bearer an archer: put on after the archery phase (pitfalls, "Archers get in the way").
		scn.AttachCardsTo(savage, bow);
		assertAttachedTo(bow, savage);
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{boromir, hillman});

		assertAttachedTo(bow, savage);
		assertAttachedTo(greataxe, hillman);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.IsSiteControlled(site1));

		// 13 + 1 for the Bow against 8: the Savage wins bearing a ranged weapon and no hand weapon.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage));

		// 9 + 2 for the Great Axe against 7: another [men] Man wins bearing a hand weapon.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void DecliningTheOfferTakesNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var greataxe = scn.GetShadowCard("greataxe");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, greataxe);
		assertAttachedTo(greataxe, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertAttachedTo(greataxe, savage);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsSiteControlled(site1));

		scn.PassCurrentPhaseActions();
		// 13 + 2 for the Great Axe against 8.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(savage));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinOfTheTurnTakesASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// Player 1's third turn: both markers are on site 3, so sites 1 and 2 can be taken.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, poleaxe);
		assertAttachedTo(poleaxe, savage);
		// Fierce only arranges a second skirmish.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertAttachedTo(poleaxe, savage);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 13 + 2 for the Poleaxe against 8.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(savage));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(savage));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
