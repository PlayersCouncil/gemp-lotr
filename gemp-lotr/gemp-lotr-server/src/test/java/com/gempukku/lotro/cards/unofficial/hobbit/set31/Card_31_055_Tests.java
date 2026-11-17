package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_055_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer (a companion) and no
	// card in the Ring slot.  The minions are assigned to Dwarves of strength 7, whom they beat without
	// overwhelming them.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wicked", "31_55");
					// Spiders of two cultures.
					put("fat", "31_51");         // Fat Spider: [spider] Spider, strength 8; a Regroup ability, not used
					put("shelob", "8_26");       // Shelob, Last Child of Ungoliant: [gollum] Spider, strength 8; damage +1 in a fierce skirmish only
					// Orcs of two cultures.
					put("veteran", "32_43");     // Orkish Veteran: [sauron] Orc; archer at site 9 only, a Regroup ability, not used
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, no text
					put("river", "31_50");       // Enchanted River: a [spider] condition, no Spider; texts that do not reach a skirmish
					// Companions: with Bilbo, five or six.
					put("balin", "30_2");        // Balin, Brother of Dwalin: strength 7; a Fellowship ability, not used
					put("dwalin", "30_9");       // Dwalin, Brother of Balin: strength 7, damage +1; a Maneuver ability, not used
					put("nori", "30_15");        // Nori, Of the Blue Mountains: a Maneuver ability, not used
					put("gloin", "30_11");       // Glóin, Father of Gimli: a Skirmish ability, not used
					put("thorin", "30_19");      // Thorin, Oakenshield: +1 for each [dwarven] follower he bears (none)
					put("betsy", "33_19");       // Betsy Butterbur: an ally, a character beside the companions who is none
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WickedSpiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Wicked Spider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Spider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br><b>Skirmish:</b> Discard an Orc to make a Spider strength +3 (or +5 if you
		 * spot 6 companions) until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wicked");

		assertEquals("Wicked Spider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SPIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishDiscardsAnOrcToMakeASpiderStrengthPlus3UntilTheRegroupPhaseWithFiveCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wicked = scn.GetShadowCard("wicked");
		var fat = scn.GetShadowCard("fat");
		var shelob = scn.GetShadowCard("shelob");
		var veteran = scn.GetShadowCard("veteran");
		var troop = scn.GetShadowCard("troop");
		var river = scn.GetShadowCard("river");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var nori = scn.GetFreepsCard("nori");
		var gloin = scn.GetFreepsCard("gloin");
		var betsy = scn.GetFreepsCard("betsy");
		// Five companions with Bilbo, and an ally beside them.
		scn.MoveCompanionsToTable(balin, dwalin, nori, gloin);
		scn.MoveAlliesToTable(betsy);
		scn.MoveCardsToSupportArea(river);

		scn.StartGame();
		scn.MoveMinionsToTable(wicked, fat, shelob, veteran, troop);
		assertInZone(Zone.FREE_CHARACTERS, balin, dwalin, nori, gloin);
		assertInZone(Zone.SUPPORT, betsy, river);
		assertInZone(Zone.SHADOW_CHARACTERS, wicked, fat, shelob, veteran, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, wicked}, new PhysicalCardImpl[]{dwalin, fat});
		// Shelob and the Orcs are left over.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(wicked));
		scn.ShadowUseCardAction(wicked);
		// Orcs of two cultures; not a Spider.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wicked, fat, shelob));
		scn.ShadowChooseCard(veteran);
		assertInZone(Zone.DISCARD, veteran);

		// Spiders of two cultures: this one in the skirmish, Fat Spider in a skirmish still to come, and Shelob in
		// none.  Not an Orc, and not a [spider] card that is no Spider.
		assertTrue(scn.ShadowHasCardChoiceAvailable(wicked, fat, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, river));
		assertEquals(8, scn.GetStrength(fat));
		scn.ShadowChooseCard(fat);

		assertEquals(11, scn.GetStrength(fat));
		assertEquals(7, scn.GetStrength(wicked));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force in Fat Spider's own skirmish, the next phase.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(11, scn.GetStrength(fat));

		// Gone at the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SHADOW_CHARACTERS, fat);
		assertEquals(8, scn.GetStrength(fat));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MakesASpiderStrengthPlus5WithSixCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wicked = scn.GetShadowCard("wicked");
		var fat = scn.GetShadowCard("fat");
		var troop = scn.GetShadowCard("troop");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var nori = scn.GetFreepsCard("nori");
		var gloin = scn.GetFreepsCard("gloin");
		var thorin = scn.GetFreepsCard("thorin");
		// Six companions with Bilbo.
		scn.MoveCompanionsToTable(balin, dwalin, nori, gloin, thorin);

		scn.StartGame();
		scn.MoveMinionsToTable(wicked, fat, troop);
		assertInZone(Zone.FREE_CHARACTERS, balin, dwalin, nori, gloin, thorin);
		assertInZone(Zone.SHADOW_CHARACTERS, wicked, fat, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, wicked);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		scn.ShadowUseCardAction(wicked);
		// The one Orc is discarded unasked.
		assertInZone(Zone.DISCARD, troop);
		assertTrue(scn.ShadowHasCardChoiceAvailable(wicked, fat));
		assertEquals(7, scn.GetStrength(wicked));
		scn.ShadowChooseCard(wicked);

		assertEquals(12, scn.GetStrength(wicked));
		assertEquals(8, scn.GetStrength(fat));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoOrcToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wicked = scn.GetShadowCard("wicked");
		var fat = scn.GetShadowCard("fat");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(wicked, fat);
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.SHADOW_CHARACTERS, wicked, fat);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, wicked);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wicked));
	}
}
