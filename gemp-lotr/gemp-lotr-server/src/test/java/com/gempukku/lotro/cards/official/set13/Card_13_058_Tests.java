package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wild", "13_58");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion; a regroup ability only
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: a [wraith] Gollum; his trigger answers a Nazgûl played
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum
					put("faramir", "12_47");    // Faramir, Dúnadan of Gondor: unbound [gondor] companion, vitality 3; a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] companion, vitality 3; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, no companion; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WildLightofMadnessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Wild Light of Madness
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot Gollum to make the Free Peoples player choose to either add a burden or exert a companion.
		*/

		var scn = GetScenario();

		var wild = scn.GetFreepsCard("wild");

		assertEquals("Wild Light of Madness", wild.getBlueprint().getTitle());
		assertNull(wild.getBlueprint().getSubtitle());
		assertFalse(wild.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, wild.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, wild.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, wild.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(wild, Timeword.SHADOW));
		assertEquals(1, wild.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGollumSpottedTheFreePeoplesPlayerChoosesToExertACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wild = scn.GetShadowCard("wild");
		var gollum = scn.GetShadowCard("gollum");
		var faramir = scn.GetFreepsCard("faramir");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(faramir, gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(wild);

		scn.StartGame();
		assertInZone(Zone.HAND, wild);
		// Gimli, vitality 3, exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(gollum);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(wild));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.ShadowPlayCard(wild);

		// The Free Peoples player is the one asked, between the two.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Exert a companion");
		// Any companion able to exert, the Ring-bearer among them; not the exhausted Gimli or the ally Grimir.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, faramir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(faramir);

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, wild);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheWraithGollumIsSpottedAndTheFreePeoplesPlayerMayChooseToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wild = scn.GetShadowCard("wild");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var faramir = scn.GetFreepsCard("faramir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(wild);

		scn.StartGame();
		assertInZone(Zone.HAND, wild);
		scn.MoveMinionsToTable(wraithgollum);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(wild));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(wild);

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertInZone(Zone.DISCARD, wild);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedABurdenIsAddedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wild = scn.GetShadowCard("wild");
		var gollum = scn.GetShadowCard("gollum");
		var faramir = scn.GetFreepsCard("faramir");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(faramir);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(wild);

		scn.StartGame();
		assertInZone(Zone.HAND, wild);
		// Frodo (vitality 4) and Faramir (vitality 3) exhausted; the ally Grimir could still exert, but is no companion.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(faramir, 2);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(faramir));
		scn.MoveMinionsToTable(gollum);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.ShadowPlayCard(wild);

		// No companion can exert: the burden, the one choice that can be made in full, is added with no question.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertInZone(Zone.DISCARD, wild);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithAGollumMinionWhoIsNoGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wild = scn.GetShadowCard("wild");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(wild);

		scn.StartGame();
		assertInZone(Zone.HAND, wild);
		scn.MoveMinionsToTable(shelob);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wild));

		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(wild));
	}
}
