package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_331_ErrataTests
{
	// The Fellowship block path with this Ettenmoors as site 2.  Each test moves there from site 1.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4; a maneuver ability
					put("guard", "1_7");          // Dwarf Guard: [dwarven] companion, strength 4, vitality 2; needs a Dwarf to be played, so cheated in
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: [elven] ally, strength 8, vitality 4; heals site 3 allies at the start of the turn
					put("oathbreaker", "8_41");   // Oathbreaker: [gondor] Wraith companion, enduring, vitality 3; a skirmish ability costing 3 cards from hand

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] minion, strength 13, vitality 3, no text
					put("runner", "1_178");       // Goblin Runner: [moria] minion, strength 5, vitality 1; its only text is on being played
					put("party", "1_158");        // Uruk-hai Raiding Party: [isengard] minion, strength 9, vitality 3; damage +1 only
					put("attea", "8_79");         // Ulaire Attea, Thrall of The One: [ringwraith] minion, enduring, vitality 3; a regroup ability
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "51_331");
					put("site3", "1_341");
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
	public void EttenmoorsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Ettenmoors
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Plains.</b> <b>Skirmish:</b> Exert your character (except an enduring character), to make it
		 * strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Ettenmoors", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachPlayerMayExertTheirOwnCharacterButNotAnEnduringOneToMakeItStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var elrond = scn.GetFreepsCard("elrond");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(aragorn, guard, oathbreaker);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);
		var ettenmoors = scn.GetCurrentSite();
		// Site 2 is played from the Shadow player's adventure deck: his copy of this card.
		assertEquals("51_331", ettenmoors.getBlueprintId());
		scn.MoveMinionsToTable(troop, party, attea);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(4, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring
		assertEquals(6, scn.GetStrength(oathbreaker));
		assertTrue(scn.FreepsActionAvailable(ettenmoors));

		// The Free Peoples player's characters, in the skirmish or not, companion or ally; not his enduring companion,
		// and not the other player's minions.
		scn.FreepsUseCardAction(ettenmoors);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo, guard, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(oathbreaker, troop, party, attea));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(13, scn.GetStrength(troop));

		// The Shadow player may use it too, on his own minions: in the skirmish or not, of either culture, and not his
		// enduring Nazgul.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(ettenmoors));
		scn.ShadowUseCardAction(ettenmoors);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, party));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attea, aragorn, frodo, guard, elrond, oathbreaker));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(party));
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(10, scn.GetStrength(aragorn));

		// No limit: the same character can be exerted again for another +2.
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(12, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void StrengthBonusLastsForTheSkirmishPhaseItIsUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);
		var ettenmoors = scn.GetCurrentSite();
		scn.MoveMinionsToTable(troop, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{guard, runner});
		scn.FreepsResolveSkirmish(aragorn);

		// In Aragorn's skirmish, the Dwarf Guard, who skirmishes next, is made strength +2.
		assertEquals(4, scn.GetStrength(guard));
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));

		// Aragorn (8) loses to the Troop (13); the Guard is still +2 to the end of this skirmish phase.
		scn.ShadowPass();
		assertEquals(6, scn.GetStrength(guard));
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// In the Guard's own skirmish phase the bonus is gone: 4 against the Runner's 5.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, runner));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenOnlyAnEnduringCharacterOfYoursCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var elrond = scn.GetFreepsCard("elrond");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, guard, oathbreaker);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);
		var ettenmoors = scn.GetCurrentSite();
		scn.MoveMinionsToTable(troop);
		// Every character of the Free Peoples player is exhausted but the enduring Oathbreaker.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(elrond, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ettenmoors));

		// The Shadow player, whose minion can exert, is offered it in the same skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ettenmoors));
		assertEquals(0, scn.GetWoundsOn(oathbreaker));
	}
}
