package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("setup", "18_33");
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion; a regroup ability used only on purpose
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion; his texts answer a Nazgul played and an assignment ability
					put("smeagol", "6_45");         // Smeagol, Poor Creature: [gollum] companion; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] companion; an assignment ability used only on purpose
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: [gollum] minion who is no Gollum
					put("deagol", "13_46");         // Deagol, Fateful Finder: [gollum] Free Peoples follower, no Smeagol; a skirmish ability only
					put("worker", "15_135");        // Rohan Worker: [rohan] follower; texts answer a transfer and a win only
					put("army", "18_47");           // Elendil's Army: [gondor] follower; an Aid and nothing else
					put("saruman", "17_38");        // Saruman, Servant of Sauron: [isengard] Shadow follower; texts answer an Aid and a win only
					put("barliman", "1_70");        // Barliman Butterbur: [gandalf] ally, no follower; a fellowship ability only
					put("stairs", "1_24");          // Stairs of Khazad-dum: [dwarven] condition in the support area; reads a Dwarf skirmishing
					put("bloodlust", "1_144");      // Uruk Bloodlust: [isengard] Shadow condition in the support area; a response to an Uruk-hai win
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SetUpStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Set Up
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot Gollum or Sméagol to return a follower to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("setup");

		assertEquals("Set Up", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGollumSpottedAFollowerOfEitherPlayerIsOfferedAndTheShadowOneGoesToTheShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var setup = scn.GetShadowCard("setup");
		var gollum = scn.GetShadowCard("gollum");
		var saruman = scn.GetShadowCard("saruman");
		var bloodlust = scn.GetShadowCard("bloodlust");
		var worker = scn.GetFreepsCard("worker");
		var army = scn.GetFreepsCard("army");
		var barliman = scn.GetFreepsCard("barliman");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToHand(setup);
		scn.MoveCardsToSupportArea(worker, army, stairs, saruman, bloodlust);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, worker, army, stairs, saruman, bloodlust, barliman);
		scn.MoveMinionsToTable(gollum);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(setup));
		scn.ShadowPlayCard(setup);

		// A follower of each player, of three cultures; an ally, a Free Peoples condition and a Shadow condition
		// in the support areas are no followers.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, army, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman, stairs, bloodlust, gollum));
		scn.ShadowChooseCard(saruman);

		// Saruman is the Shadow player's own: back to his hand, not the Free Peoples player's.
		assertTrue(scn.GetShadowHand().contains(saruman));
		assertFalse(scn.GetFreepsHand().contains(saruman));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, worker, army, stairs, bloodlust, barliman);
		assertInDiscard(setup);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSmeagolSpottedAFreePeoplesFollowerGoesToTheFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var setup = scn.GetShadowCard("setup");
		var smeagol = scn.GetFreepsCard("smeagol");
		var worker = scn.GetFreepsCard("worker");
		var army = scn.GetFreepsCard("army");
		scn.MoveCardsToHand(setup);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(worker, army);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, worker, army);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(setup));
		scn.ShadowPlayCard(setup);
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, army));
		scn.ShadowChooseCard(worker);

		assertTrue(scn.GetFreepsHand().contains(worker));
		assertFalse(scn.GetShadowHand().contains(worker));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.SUPPORT, army);
		assertInDiscard(setup);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheWraithGollumIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var setup = scn.GetShadowCard("setup");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var worker = scn.GetFreepsCard("worker");
		var army = scn.GetFreepsCard("army");
		scn.MoveCardsToHand(setup);
		scn.MoveCardsToSupportArea(worker, army);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, worker, army);
		scn.MoveMinionsToTable(wraithgollum);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(setup));
		scn.ShadowPlayCard(setup);
		scn.ShadowChooseCard(army);

		assertTrue(scn.GetFreepsHand().contains(army));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, worker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var setup = scn.GetShadowCard("setup");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var worker = scn.GetFreepsCard("worker");
		var army = scn.GetFreepsCard("army");
		scn.MoveCardsToHand(setup);
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.MoveCardsToSupportArea(worker, army);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, worker, army);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(setup));
		scn.ShadowPlayCard(setup);
		scn.ShadowChooseCard(worker);

		assertTrue(scn.GetFreepsHand().contains(worker));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, army);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNeitherGollumNorSmeagolToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var setup = scn.GetShadowCard("setup");
		var shelob = scn.GetShadowCard("shelob");
		var deagol = scn.GetFreepsCard("deagol");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCardsToHand(setup);
		scn.MoveCardsToSupportArea(deagol, worker);
		assertInHand(setup);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, deagol, worker);
		scn.MoveMinionsToTable(shelob);
		scn.FreepsPass();

		// A [gollum] minion who is no Gollum, and a [gollum] Free Peoples card who is no Smeagol.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(setup));
		assertInHand(setup);
	}

	@Test
	public void WithNoFollowerInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var setup = scn.GetShadowCard("setup");
		var gollum = scn.GetShadowCard("gollum");
		var barliman = scn.GetFreepsCard("barliman");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToHand(setup);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stairs, barliman);
		scn.MoveMinionsToTable(gollum);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(setup));
		scn.ShadowPlayCard(setup);

		assertInDiscard(setup);
		assertInZone(Zone.SUPPORT, stairs, barliman);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
