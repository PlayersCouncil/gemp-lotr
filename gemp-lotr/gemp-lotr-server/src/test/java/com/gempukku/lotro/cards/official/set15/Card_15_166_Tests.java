package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_166_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("poised", "15_166");

					// Murderous Uruk: an [uruk-hai] Uruk-hai, strength 8, damage +1 and muster (a regroup matter) only.
					put("uruk", "11_195");
					// Saruman, Agent of the Dark Lord: an [uruk-hai] minion who is no Uruk-hai (a Wizard), strength 8, a
					// lurker.  His skirmish ability is never used, and no companion is killed for his heal.
					put("saruman", "12_144");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, damage +1 and no other text.
					put("cuguard", "10_81");
					// White Hand Destroyer: an [uruk-hai] Uruk-hai, fierce, strength 10, vitality 3; hunter 3 only for
					// each site its owner controls (none here).
					put("destroyer", "17_124");
					// Weapons of Control: another [uruk-hai] support-area condition that holds [uruk-hai] tokens; its
					// skirmish ability is never used.
					put("weapons", "13_177");

					// Gimli, Son of Gloin: strength 6, vitality 3.  His skirmish ability is never used.
					put("gimli", "1_13");
					// Boromir, Son of Denethor: strength 7, vitality 3.  His skirmish ability is never used.
					put("boromir", "1_97");
					// Aragorn, Ranger of the North, strength 8, vitality 4; with Ranger's Sword (strength +2, damage +1).
					put("aragorn", "1_89");
					put("sword", "1_112");
					// Thrarin, Dwarven Smith: a Dwarf ally; his maneuver ability lets him skirmish.
					put("thrarin", "1_27");
					// Dwarven Armor: its bearer is overwhelmed only at triple strength.
					put("armor", "1_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PoisedforAssaultStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Poised for Assault
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a companion loses a skirmish involving an [uruk-hai] minion, place an [uruk-hai] token here.<br><b>Skirmish:</b> Remove 2 [uruk-hai] tokens from here to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("poised");

		assertEquals("Poised for Assault", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionLosingToAnUrukHaiUrukHaiOrToAnUrukHaiWizardPlacesAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, boromir);

		var poised = scn.GetShadowCard("poised");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(poised);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, saruman);
		assertEquals(0, scn.GetCultureTokensOn(poised));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, uruk},
				new PhysicalCardImpl[]{gimli, saruman});

		// The Murderous Uruk's 8 beats Boromir's 7.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetCultureTokensOn(poised));

		// Saruman's 8 beats Gimli's 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetCultureTokensOn(poised));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoTokenForACompanionWhoWinsForALossToASauronUrukHaiOrForAnAllyWhoLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(thrarin, armor);

		var poised = scn.GetShadowCard("poised");
		var uruk = scn.GetShadowCard("uruk");
		var cuguard = scn.GetShadowCard("cuguard");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(poised);

		scn.StartGame();
		// Vitality raised by hand only so that Thrarin lives through his exertion and his loss.
		scn.ApplyAdHocModifier(new VitalityModifier(null, thrarin, null, 2));
		scn.MoveMinionsToTable(uruk, cuguard, saruman);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, thrarin);
		assertEquals(0, scn.GetCultureTokensOn(poised));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uruk},
				new PhysicalCardImpl[]{gimli, cuguard},
				new PhysicalCardImpl[]{thrarin, saruman});

		// Aragorn's 10 beats the Murderous Uruk's 8: a companion who wins a skirmish involving an [uruk-hai] minion.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetCultureTokensOn(poised));

		// The Guard's 9 beats Gimli's 6: a loss to a [sauron] Uruk-hai.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetCultureTokensOn(poised));

		// Saruman's 8 beats Thrarin's 4 (not tripled, so not overwhelmed): an ally who loses.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheSameCompanionLosingAgainInTheFierceRoundPlacesASecondToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var poised = scn.GetShadowCard("poised");
		var destroyer = scn.GetShadowCard("destroyer");
		scn.MoveCardsToSupportArea(poised);

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer);
		assertEquals(0, scn.GetCultureTokensOn(poised));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, destroyer);
		scn.FreepsResolveSkirmish(aragorn);
		// The Destroyer's 10 beats Aragorn's 8.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetCultureTokensOn(poised));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, destroyer);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetCultureTokensOn(poised));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemoves2TokensToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var poised = scn.GetShadowCard("poised");
		var uruk = scn.GetShadowCard("uruk");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, and site 1 is behind them.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(poised);
		scn.AddTokensToCard(poised, 3);
		scn.MoveMinionsToTable(uruk);
		assertEquals(3, scn.GetCultureTokensOn(poised));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowControls(site1));
		assertTrue(scn.ShadowActionAvailable(poised));
		scn.ShadowUseCardAction(poised);

		// 3 - 2 tokens, and the lowest site behind every marker is his.
		assertEquals(1, scn.GetCultureTokensOn(poised));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedInTheAssignmentPhaseAndOnTurnOneUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var poised = scn.GetShadowCard("poised");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToSupportArea(poised);

		scn.StartGame();
		scn.AddTokensToCard(poised, 2);
		scn.MoveMinionsToTable(uruk);
		assertEquals(2, scn.GetCultureTokensOn(poised));

		// The phase before: the Shadow player's assignment actions.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(poised));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		// Turn 1: the Shadow player's marker is still on site 1, so no site can be taken.  The ability has a cost
		// and is offered all the same; the tokens are paid and nothing is taken.
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowActionAvailable(poised));
		scn.ShadowUseCardAction(poised);
		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithOneTokenHereThoughAnotherUrukHaiCardHoldsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var poised = scn.GetShadowCard("poised");
		var weapons = scn.GetShadowCard("weapons");
		var uruk = scn.GetShadowCard("uruk");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(poised, weapons);
		scn.AddTokensToCard(poised, 1);
		scn.AddTokensToCard(weapons, 2);
		scn.MoveMinionsToTable(uruk);
		assertEquals(1, scn.GetCultureTokensOn(poised));
		assertEquals(2, scn.GetCultureTokensOn(weapons));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(poised));
		assertEquals(1, scn.GetCultureTokensOn(poised));
		assertEquals(2, scn.GetCultureTokensOn(weapons));
	}
}
