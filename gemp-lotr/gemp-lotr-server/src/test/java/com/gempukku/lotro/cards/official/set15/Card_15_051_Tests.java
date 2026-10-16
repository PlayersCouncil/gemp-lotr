package com.gempukku.lotro.cards.official.set15;

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

public class Card_15_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strike", "15_51");
					// Sméagol, Poor Creature: the Free Peoples player's [gollum] companion; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: a [shire] companion; an assignment ability used only on purpose
					put("shiresmeagol", "103_116");
					// Gollum, Nasty Treacherous Creature: a [gollum] minion, cost 2; a regroup ability only
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: a [wraith] minion; his texts answer a Nazgûl played, and an assignment ability
					put("wraithgollum", "103_66");
					// Shelob, Last Child of Ungoliant: a [gollum] minion of another title; her text is about fierce skirmishes
					put("shelob", "8_26");
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc to bear a Goblin Scimitar

					// For the Shadow player's draw deck:
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc minion with no text, cost 6
					put("poison", "8_28");       // Spider Poison: a [gollum] Shadow possession that plays to the support area
					put("scimitar", "1_180");    // Goblin Scimitar: a [moria] Shadow possession borne by a [moria] Orc
					put("armory", "1_173");      // Goblin Armory: a [moria] Shadow condition of the support area, cost 0
					put("lethim", "7_63");       // Let Her Deal With Them: a [gollum] Shadow condition borne by a minion
					put("setup", "18_33");       // Set Up: a [gollum] Shadow event
					put("boat", "13_48");        // Fishing Boat: a [gollum] Free Peoples possession
					put("nicefish", "15_46");    // Nice Fish: a [gollum] Free Peoples condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuddenStrikeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Sudden Strike
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot Gollum or Sméagol to play a minion from your draw deck. If that minion is a [gollum] minion, you may also play a Shadow possession or Shadow condition from your draw deck. Shuffle your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strike");

		assertEquals("Sudden Strike", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithSmeagolSpottedAGollumMinionIsPlayedAndThenAShadowPossessionOrCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var strike = scn.GetShadowCard("strike");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var poison = scn.GetShadowCard("poison");
		var scimitar = scn.GetShadowCard("scimitar");
		var armory = scn.GetShadowCard("armory");
		var lethim = scn.GetShadowCard("lethim");
		var setup = scn.GetShadowCard("setup");
		var boat = scn.GetShadowCard("boat");
		var nicefish = scn.GetShadowCard("nicefish");

		scn.StartGame();
		scn.MoveCardsToHand(strike);
		scn.SetTwilight(20);
		scn.FreepsPass();
		// A [moria] Orc who could bear the Goblin Scimitar.
		scn.MoveMinionsToTable(runner);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.DECK, gollum, troop, poison, scimitar, armory, lethim, setup, boat, nicefish);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(strike));

		scn.ShadowPlayCard(strike);
		scn.ShadowDismissRevealedCards();
		// A minion: a [gollum] one and a [moria] Orc; no possession, condition or event of either side.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, scimitar, armory, lethim, setup, boat, nicefish));
		scn.ShadowChooseCardBPFromSelection(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		// Gollum is a [gollum] minion: a Shadow possession or Shadow condition may follow, of the support area or
		// borne; not a minion, an event, nor a Free Peoples possession or condition.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(poison, scimitar, armory, lethim));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, setup, boat, nicefish));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(armory);

		assertInZone(Zone.SUPPORT, armory);
		assertInZone(Zone.DECK, troop, poison, scimitar, lethim, setup, boat, nicefish);
		assertInDiscard(strike);
		// Each card played out of the looked-through deck shuffles it as it leaves, and each play shuffles it
		// again as the look ends (pitfalls, "Whether a deck was shuffled"): 2 for the minion, 2 for the condition.
		assertEquals(shadowShuffles + 4, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithGollumSpottedAnOrcIsPlayedAndNothingMoreIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetShadowCard("strike");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		scn.MoveCardsToHand(strike);
		scn.SetTwilight(20);
		scn.FreepsPass();
		// Gollum is spotted, and is a [gollum] minion in play that is not the minion the event plays.
		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.DECK, troop, armory);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(strike);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(troop);

		// A [moria] Orc was played: no second play is offered, and the condition stays in the deck.
		assertInZone(Zone.SHADOW_CHARACTERS, troop, gollum);
		assertInZone(Zone.DECK, armory);
		assertInDiscard(strike);
		// Once as the Orc leaves the looked-through deck and once as the look ends: no second look.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheShireSmeagolSpottedTheSecondPlayMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(shiresmeagol);

		var strike = scn.GetShadowCard("strike");
		var shelob = scn.GetShadowCard("shelob");
		var armory = scn.GetShadowCard("armory");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.MoveCardsToHand(strike);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInZone(Zone.DECK, shelob, armory, poison);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(strike));

		scn.ShadowPlayCard(strike);
		scn.ShadowDismissRevealedCards();
		// Shelob, a [gollum] minion who is not Gollum.
		scn.ShadowChooseCardBPFromSelection(shelob);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armory, poison));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.DECK, armory, poison);
		assertInDiscard(strike);
		// 2 for the minion, and 1 for the look that played nothing.
		assertEquals(shadowShuffles + 3, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithShelobAloneAndPlayableOnceTheWraithGollumIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetShadowCard("strike");
		var shelob = scn.GetShadowCard("shelob");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToHand(strike);
		scn.SetTwilight(20);
		scn.FreepsPass();
		// A [gollum] minion of another title.
		scn.MoveMinionsToTable(shelob);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strike));

		// Gollum, Half a Wraith Himself: a [wraith] Gollum.
		scn.MoveMinionsToTable(wraithgollum);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(strike));

		scn.ShadowPlayCard(strike);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInDiscard(strike);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
