package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_031_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The skirmishes are those of turn 1, at The Troll Hoard 30_50 (site 2); the minions are cheated in.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "32_31");
					// A second copy: its own exertion, and its own limit.
					put("troll2", "32_31");
					// [moria] minions of other races, and a minion of another culture.
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("bats", "32_30");        // Dark Bats: [moria] Creature, strength 4; a Shadow ability, not used
					put("marauder", "30_38");    // Orkish Marauder: [gundabad] Orc, strength 8; texts that need 4 followers or 7 companions
					// Companions for the skirmishes.
					put("balin", "30_2");        // Balin, Brother of Dwalin: strength 7, vitality 3; a Fellowship ability, not used
					put("dwalin", "30_9");       // Dwalin, Brother of Balin: strength 7, vitality 3; a Maneuver ability, not used
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DemolitionTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Demolition Troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion to make a [moria] minion strength +2 (limit +2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Demolition Troll", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishExertsToMakeAMoriaMinionStrongerUpToPlus2EachSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var troop = scn.GetShadowCard("troop");
		var bats = scn.GetShadowCard("bats");
		var marauder = scn.GetShadowCard("marauder");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(balin, dwalin);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, troll2, troop, bats, marauder);
		assertInZone(Zone.SHADOW_CHARACTERS, troll, troll2, troop, bats, marauder);
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troll2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, troll}, new PhysicalCardImpl[]{dwalin, troop});
		// The second copy, the Bats and the Marauder are left over.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troll));
		scn.ShadowUseCardAction(troll);
		// The exertion is this copy's own.
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troll2));
		// [moria] minions of three races, in this skirmish, in a skirmish to come and in none; not an Orc of
		// another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll, troll2, troop, bats));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(marauder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);
		// 13 + 2
		assertEquals(15, scn.GetStrength(troop));

		// The card has given +2 this skirmish: offered, the exertion paid, and nothing more for another minion.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(troll));
		scn.ShadowUseCardAction(troll);
		assertEquals(2, scn.GetWoundsOn(troll));
		scn.ShadowChooseCard(bats);
		assertEquals(4, scn.GetStrength(bats));
		assertEquals(15, scn.GetStrength(troop));

		// The limit is each copy's own: the second copy gives +2 of its own in the same skirmish.
		scn.FreepsPass();
		scn.ShadowUseCardAction(troll2);
		assertEquals(1, scn.GetWoundsOn(troll2));
		scn.ShadowChooseCard(troop);
		// 13 + 2 + 2
		assertEquals(17, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonuses are gone, and the second copy gives +2 again.  The
		// first copy, exhausted (2 wounds of 3), cannot pay and is not offered.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));
		assertTrue(scn.ShadowActionAvailable(troll2));
		scn.ShadowUseCardAction(troll2);
		assertEquals(2, scn.GetWoundsOn(troll2));
		assertEquals(2, scn.GetWoundsOn(troll));
		scn.ShadowChooseCard(troop);
		assertEquals(15, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
