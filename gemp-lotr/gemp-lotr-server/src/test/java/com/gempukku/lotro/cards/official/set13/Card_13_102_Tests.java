package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "13_102");
					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7; the current site gains plains
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: [men] Hobbit, strength 3; abilities used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: ally who can be made to take part in skirmishes

					// Cards that carry culture tokens, one of each side
					put("path", "8_42");        // A Path Appointed: [gondor] support-area condition that holds [gondor] tokens
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession that holds [raider] tokens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WornBattleaxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Worn Battleaxe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [men] minion.<br>While you can spot a culture token, bearer is strength +1 for each companion assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Worn Battleaxe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnAMenMinionOfAnyRaceAndNotOnAManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var footman = scn.GetShadowCard("footman");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		scn.MoveMinionsToTable(footman, ted, brigand);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(axe));
		scn.ShadowPlayCard(axe);

		// A [men] Man and a [men] Hobbit; not the [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		assertAttachedTo(axe, ted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileAGondorTokenCanBeSpottedBearerIsStrengthPlus1ForEachCompanionAssignedToAnySkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(path);
		scn.AddTokensToCard(path, 1);

		var axe = scn.GetShadowCard("axe");
		var footman = scn.GetShadowCard("footman");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(footman, ted, brigand);
		scn.AttachCardsTo(footman, axe);
		assertAttachedTo(axe, footman);
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin is made able to be assigned like a companion.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		// No companion is assigned yet: 7 printed + 2 for the axe.
		assertEquals(9, scn.GetStrength(footman));

		scn.SkipToAssignments();
		// Aragorn on the bearer, Gimli on another minion, the ally Thrarin on Ted; Frodo assigned to nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, footman},
				new PhysicalCardImpl[]{gimli, brigand},
				new PhysicalCardImpl[]{thrarin, ted});

		// Two companions assigned: +2. Not the ally, nor the minions, nor Frodo (12 if every character in a
		// skirmish counted, 10 if only the bearer's own skirmish did).
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(11, scn.GetStrength(footman));
		// Ted is a [men] minion who does not bear it.
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(8, scn.GetStrength(brigand));

		// Still so in the bearer's own skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(11, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARaiderTokenOnAShadowCardAlsoCountsAndOneCompanionAssignedIsPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var axe = scn.GetShadowCard("axe");
		var footman = scn.GetShadowCard("footman");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(ships, 1);

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(ships));
		scn.MoveMinionsToTable(footman);
		scn.AttachCardsTo(footman, axe);
		assertAttachedTo(axe, footman);
		scn.SkipToAssignments();

		assertEquals(9, scn.GetStrength(footman));
		scn.FreepsAssignToMinions(aragorn, footman);

		// One companion assigned, Gimli not: 7 + 2 + 1.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(10, scn.GetStrength(footman));
	}

	@Test
	public void WithNoCultureTokenInPlayBearerGetsNothingForTheCompanionsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// A card that holds culture tokens, with none on it.
		scn.MoveCardsToSupportArea(path);

		var axe = scn.GetShadowCard("axe");
		var footman = scn.GetShadowCard("footman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(footman, brigand);
		scn.AttachCardsTo(footman, axe);
		assertAttachedTo(axe, footman);
		scn.SkipToAssignments();

		assertEquals(9, scn.GetStrength(footman));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, footman},
				new PhysicalCardImpl[]{gimli, brigand});

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(9, scn.GetStrength(footman));
	}
}
