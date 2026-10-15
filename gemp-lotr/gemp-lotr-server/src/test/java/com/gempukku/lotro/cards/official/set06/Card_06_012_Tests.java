package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("agility", "6_12");
					// Lorien Elf: Elf companion, strength 4, vitality 2; "To play, spot an Elf", so it is cheated in
					put("elf", "1_53");
					// Silinde, Elf of Mirkwood: Elf ally, home site 3, strength 5, vitality 2; has the text of site 3
					// (Rivendell Valley: a fellowship action) while it can be spotted
					put("silinde", "1_60");
					// Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability used
					// only on purpose
					put("boromir", "1_97");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability
					// used only on purpose
					put("aragorn", "1_89");
					// Final Count: another [elven] support-area condition, which gives itself [elven] tokens when
					// Legolas wins a skirmish (no Legolas here)
					put("count", "4_69");
					// Strength of Arms: an [elven] support-area condition to play after this one; its trigger answers
					// an Elf winning a skirmish, and its skirmish ability is used only on purpose
					put("arms", "4_82");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Footman of Dunland: [men] Man, strength 7, vitality 3; the site gains plains
					put("footman", "11_82");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AgilityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Agility
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area. When you play this condition, you may spot an Elf to place 2 [elven] tokens here.
		 * 	<b>Skirmish:</b> Discard this condition or remove an [elven] token from here to make a minion skirmishing an Elf strength -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("agility");

		assertEquals("Agility", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedBesideAnElfCompanionMayPlaceTwoTokensHereAndAnotherCardPlayedLaterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agility = scn.GetFreepsCard("agility");
		var elf = scn.GetFreepsCard("elf");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToSupportArea(count);
		scn.MoveCardsToHand(agility, arms);

		scn.StartGame();
		scn.AddTokensToCard(count, 1);

		assertEquals(1, scn.GetCultureTokensOn(count));
		assertInZone(Zone.HAND, agility);

		scn.FreepsPlayCard(agility);
		assertInZone(Zone.SUPPORT, agility);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(agility));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(agility));
		// "Here": the other [elven] condition gets none.
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another card played once this one is in play is not "this condition".
		scn.FreepsPlayCard(arms);
		assertInZone(Zone.SUPPORT, arms);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(agility));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(0, scn.GetCultureTokensOn(arms));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsEnoughToSpotAndDecliningPlacesNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agility = scn.GetFreepsCard("agility");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(agility);

		scn.StartGame();
		scn.FreepsPlayCard(agility);

		// The ally is the only Elf there is.
		assertInZone(Zone.SUPPORT, agility);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(agility));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(agility));
		assertInZone(Zone.SUPPORT, agility);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyCompanionsWhoAreNoElvesNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agility = scn.GetFreepsCard("agility");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToHand(agility);

		scn.StartGame();
		scn.FreepsPlayCard(agility);

		assertInZone(Zone.SUPPORT, agility);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(agility));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesATokenHereToMakeAMinionSkirmishingAnElfWeakerAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agility = scn.GetFreepsCard("agility");
		var elf = scn.GetFreepsCard("elf");
		var boromir = scn.GetFreepsCard("boromir");
		var count = scn.GetFreepsCard("count");
		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elf, boromir);
		scn.MoveCardsToSupportArea(agility, count);

		scn.StartGame();
		scn.AddTokensToCard(agility, 2);
		scn.AddTokensToCard(count, 1);
		assertInZone(Zone.SUPPORT, agility);
		assertEquals(2, scn.GetCultureTokensOn(agility));
		assertEquals(1, scn.GetCultureTokensOn(count));
		scn.MoveMinionsToTable(savage, footman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, savage},
				new PhysicalCardImpl[]{boromir, troop}
		);
		scn.ShadowAssignToMinions(elf, footman);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(agility));

		scn.FreepsUseCardAction(agility);
		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("token from here"));
		scn.FreepsChooseOption("token from here");

		// In Boromir's skirmish the Troop fights a companion who is no Elf, and the two minions assigned to the
		// Elf are not skirmishing yet: the token is paid, and no minion is made weaker.
		assertEquals(1, scn.GetCultureTokensOn(agility));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(elf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(agility);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("token from here");

		// Both minions in the Elf's skirmish; not the Troop, whose skirmish is over.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);

		assertEquals(0, scn.GetCultureTokensOn(agility));
		assertInZone(Zone.SUPPORT, agility);
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(6, scn.GetStrength(footman));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoTokenHereItIsDiscardedUnaskedToMakeAMinionSkirmishingAnElfAllyWeakerForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agility = scn.GetFreepsCard("agility");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var count = scn.GetFreepsCard("count");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(agility, count);

		scn.StartGame();
		scn.AddTokensToCard(count, 1);
		assertInZone(Zone.SUPPORT, agility);
		assertEquals(0, scn.GetCultureTokensOn(agility));
		assertEquals(1, scn.GetCultureTokensOn(count));
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage, troop);
		// The fellowship moves to site 3, Silinde's home, where she can skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{silinde, savage},
				new PhysicalCardImpl[]{aragorn, troop}
		);
		scn.FreepsResolveSkirmish(silinde);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetCultureTokensOn(agility));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(agility));

		scn.FreepsUseCardAction(agility);
		// No token here (the one on Final Count is not "here"): only the discard can be paid, and nothing is
		// asked; the Savage is the one minion skirmishing an Elf.
		assertInZone(Zone.DISCARD, agility);
		assertEquals(1, scn.GetCultureTokensOn(count));
		// Read at the next decision of the same skirmish.
		assertEquals(4, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Silinde (5) beats the Savage (4) without overwhelming it; it takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);

		// A skirmish ability lasts for the skirmish phase it is used in.
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agility = scn.GetFreepsCard("agility");
		var elf = scn.GetFreepsCard("elf");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToSupportArea(agility);

		scn.StartGame();
		scn.AddTokensToCard(agility, 2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetCultureTokensOn(agility));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(agility));
	}
}
