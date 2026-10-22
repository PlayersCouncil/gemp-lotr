package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brute", "10_38");
					put("brute2", "10_38");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("raiders", "4_242");    // Raiders From the East: [raider] support-area condition that holds [raider] tokens
					put("ships1", "8_65");      // Ships of Great Draught: [raider] support-area possession
					put("ships2", "8_65");      //   that holds [raider] tokens
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] possession that holds [men] tokens

					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion; a skirmish ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally his own maneuver ability puts in a skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairBruteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Corsair Brute
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br>At the start of each skirmish involving this minion, you may remove a [raider] token to wound a companion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brute");

		assertEquals("Corsair Brute", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfEachOfHisSkirmishesMayRemoveARaiderTokenToWoundTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brute = scn.GetShadowCard("brute");
		var troop = scn.GetShadowCard("troop");
		var raiders = scn.GetShadowCard("raiders");
		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToSupportArea(raiders, ships1, ships2, fleet);
		scn.AddTokensToCard(raiders, 2);
		scn.AddTokensToCard(ships1, 1);
		// A [men] token: a token, but not a [raider] one.
		scn.AddTokensToCard(fleet, 1);

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(brute, troop);
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(ships1));
		assertEquals(0, scn.GetCultureTokensOn(ships2));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, brute}, new PhysicalCardImpl[]{boromir, troop});
		// Fierce by hand, for a second skirmish of his.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, brute, null, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowAcceptOptionalTrigger();

		// A card that has a [raider] token, a condition or a possession; not one with none, nor a [men] token.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, ships1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ships2, fleet));
		// A token must be removed once the offer is taken.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.ShadowChooseCard(raiders);

		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(ships1));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		// The one companion he is skirmishing takes one wound; the companion in the other skirmish none.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 10 against 6: Gimli loses and takes a second wound, and is exhausted.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(boromir);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.PassSkirmishActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, brute);
		scn.FreepsResolveSkirmish(gimli);

		// Each skirmish of his: offered again.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(ships1);

		assertEquals(0, scn.GetCultureTokensOn(ships1));
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		// A wound, not an exertion: the exhausted Gimli takes it and is killed.
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheStartOfAnotherMinionsSkirmishSetsOffOnlyThatMinionAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brute = scn.GetShadowCard("brute");
		var brute2 = scn.GetShadowCard("brute2");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(raiders);
		scn.AddTokensToCard(raiders, 2);

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(brute, brute2);
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, brute}, new PhysicalCardImpl[]{boromir, brute2});

		scn.FreepsResolveSkirmish(boromir);
		// The other copy's skirmish: the other copy's offer, not this one's.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoRaiderTokenInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brute = scn.GetShadowCard("brute");
		var raiders = scn.GetShadowCard("raiders");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToSupportArea(raiders, fleet);
		// The one token in play is a [men] one.
		scn.AddTokensToCard(fleet, 1);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(brute);
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertEquals(0, scn.GetCultureTokensOn(raiders));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brute);
		scn.FreepsResolveSkirmish(gimli);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
	}

	@Test
	public void SkirmishingAnAllyTheTokenIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brute = scn.GetShadowCard("brute");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(raiders);
		scn.AddTokensToCard(raiders, 2);

		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(brute);
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, brute);
		scn.FreepsResolveSkirmish(thrarin);

		// The offer has a cost that can be paid, so it is made; but an ally is no companion.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
