package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "17_82");
					put("rider2", "17_82");

					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, strength 9; +2 only at an underground site
					put("denizen2", "11_115");
					put("cavalry", "17_74");      // Orkish Cavalry: [orc] Orc, strength 8; answers an [orc] mount played (a draw, declined)
					put("halftroll", "14_12");    // Half-troll of Far Harad: [orc] minion who is no Orc, strength 8; a start-of-assignment trigger
					put("wargrider", "5_67");     // Warg-rider: [isengard] Orc, strength 5, warg-rider; fierce while not exhausted
					put("vicious", "17_92");      // Vicious Warg: [orc] mount, +4 strength, cost 3; bearer an [orc] Orc of strength 9 or less
					put("threatening", "17_91");  // Threatening Warg: [orc] mount, +2 strength; its play trigger is never reached here (cheated on)
					put("gothmog", "18_81");      // Gothmog's Warg, Leader's Mount: [orc] mount, +1 strength, cost 1
					put("relentless", "17_89");   // Relentless Warg: [orc] mount, for the other player's discard pile
					put("warwarg", "5_64");       // War-warg: an [isengard] mount, +4 strength, for a warg-rider
					put("gear", "17_90");         // Rider's Gear: an [orc] possession that is no mount (armor), +2 strength
					put("gear2", "17_90");
					put("effort", "15_106");      // Coordinated Effort: [orc] condition; answers only a Free Peoples possession played
					put("goblinrunner", "1_178"); // Goblin Runner: a [moria] card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider", "17_82");
					// Three [orc] mounts of three blueprints, each for an [orc] Orc of strength 9 or less.
					put("vicious", "17_92");   // Vicious Warg: for the hand, the only [orc] card there
					put("gothmog", "18_81");   // Gothmog's Warg, Leader's Mount, cost 1: for the discard pile
					put("threatening", "17_91"); // Threatening Warg: for the discard pile
					// Goblin Runner: a [moria] card in hand, which cannot pay
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishRiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Rider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: Each mounted [orc] Orc is strength +1.<br><b>Shadow:</b> Discard an [orc] card from your hand to play an [orc] mount from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("rider");

		assertEquals("Orkish Rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheMountPlayedIsNotTheCardDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var rider = scn.GetShadowCard("rider");
		var vicious = scn.GetShadowCard("vicious");
		var gothmog = scn.GetShadowCard("gothmog");
		var threatening = scn.GetShadowCard("threatening");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(vicious, runner);
		scn.MoveCardsToDiscard(gothmog, threatening);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		assertInHand(vicious, runner);
		assertInDiscard(gothmog, threatening);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(vicious, runner);
		assertInDiscard(gothmog, threatening);
		// 20 set + 1 companion + 2 for Ettenmoors
		assertEquals(23, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(rider));
		scn.ShadowUseCardAction(rider);
		// The Vicious Warg is the one [orc] card in hand and pays without a choice
		assertInDiscard(vicious);
		assertInHand(runner);

		// The two mounts that lay in the discard pile before the cost was paid, and not the one that lies there
		// now because it paid it (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.ShadowHasCardChoiceAvailable(gothmog, threatening));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(vicious));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(gothmog);

		// The Rider is the only Orc to bear it
		assertAttachedTo(gothmog, rider);
		assertInDiscard(vicious, threatening);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMountedOrcOrcIsStrengthPlusOneForEachRider() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var rider2 = scn.GetShadowCard("rider2");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var cavalry = scn.GetShadowCard("cavalry");
		var halftroll = scn.GetShadowCard("halftroll");
		var wargrider = scn.GetShadowCard("wargrider");
		var vicious = scn.GetShadowCard("vicious");
		var threatening = scn.GetShadowCard("threatening");
		var gothmog = scn.GetShadowCard("gothmog");
		var warwarg = scn.GetShadowCard("warwarg");
		var gear = scn.GetShadowCard("gear");

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, denizen2, cavalry, halftroll, wargrider);
		// Mounted [orc] Orcs of two titles on two mounts; an [orc] Orc bearing a possession that is no mount;
		// a mounted [isengard] Orc; an [orc] minion who is no Orc.
		scn.AttachCardsTo(denizen, vicious);
		scn.AttachCardsTo(cavalry, threatening);
		scn.AttachCardsTo(denizen2, gear);
		scn.AttachCardsTo(wargrider, warwarg);
		assertAttachedTo(vicious, denizen);
		assertAttachedTo(threatening, cavalry);
		assertAttachedTo(gear, denizen2);
		assertAttachedTo(warwarg, wargrider);

		// Without a Rider: each at its own strength and its item's.
		assertEquals(13, scn.GetStrength(denizen));    // 9 + 4
		assertEquals(10, scn.GetStrength(cavalry));    // 8 + 2
		assertEquals(11, scn.GetStrength(denizen2));   // 9 + 2
		assertEquals(9, scn.GetStrength(wargrider));   // 5 + 4
		assertEquals(8, scn.GetStrength(halftroll));

		scn.MoveMinionsToTable(rider);
		assertEquals(14, scn.GetStrength(denizen));
		assertEquals(11, scn.GetStrength(cavalry));
		assertEquals(11, scn.GetStrength(denizen2));
		assertEquals(9, scn.GetStrength(wargrider));
		assertEquals(8, scn.GetStrength(halftroll));
		// The Rider is an [orc] Orc too, and not mounted yet.
		assertEquals(6, scn.GetStrength(rider));

		scn.AttachCardsTo(rider, gothmog);
		assertAttachedTo(gothmog, rider);
		assertEquals(8, scn.GetStrength(rider));       // 6 + 1 + 1

		// A second Rider, not mounted: each mounted [orc] Orc is +1 for each Rider.
		scn.MoveMinionsToTable(rider2);
		assertEquals(6, scn.GetStrength(rider2));
		assertEquals(9, scn.GetStrength(rider));
		assertEquals(15, scn.GetStrength(denizen));
		assertEquals(12, scn.GetStrength(cavalry));
		assertEquals(11, scn.GetStrength(denizen2));
		assertEquals(9, scn.GetStrength(wargrider));
		assertEquals(8, scn.GetStrength(halftroll));
	}

	@Test
	public void ShadowAbilityDiscardsAnOrcCardFromHandToPlayAnOrcMountFromTheDiscardPileOnAnOrcOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var halftroll = scn.GetShadowCard("halftroll");
		var wargrider = scn.GetShadowCard("wargrider");
		var vicious = scn.GetShadowCard("vicious");
		var gothmog = scn.GetShadowCard("gothmog");
		var warwarg = scn.GetShadowCard("warwarg");
		var gear = scn.GetShadowCard("gear");
		var gear2 = scn.GetShadowCard("gear2");
		var effort = scn.GetShadowCard("effort");
		var goblinrunner = scn.GetShadowCard("goblinrunner");
		var hisRelentless = scn.GetFreepsCard("relentless");

		// In hand: an [orc] minion, an [orc] possession, an [orc] condition, and a [moria] card.
		scn.MoveCardsToHand(denizen2, gear, effort, goblinrunner);
		// In the discard pile: two [orc] mounts, an [orc] possession that is no mount, and an [isengard] mount
		// that the Warg-rider in play could bear.  The other player's [orc] mount lies in his own pile.
		scn.MoveCardsToDiscard(vicious, gothmog, gear2, warwarg, hisRelentless);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, denizen, halftroll, wargrider);
		scn.SetTwilight(10);
		assertInHand(denizen2, gear, effort, goblinrunner);
		assertInDiscard(vicious, gothmog, gear2, warwarg, hisRelentless);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(rider));
		scn.ShadowUseCardAction(rider);

		// The cost: any [orc] card from hand, of whatever type.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen2, gear, effort));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(goblinrunner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(effort);
		assertInDiscard(effort);
		assertInHand(denizen2, gear, goblinrunner);

		// The [orc] mounts of this player's discard pile only.
		assertTrue(scn.ShadowHasCardChoiceAvailable(vicious, gothmog));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gear2, warwarg, hisRelentless, effort));
		scn.ShadowChooseCardBPFromSelection(vicious);

		// The Vicious Warg's own bearer: an [orc] Orc of strength 9 or less.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, wargrider));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertAttachedTo(vicious, denizen);
		assertInDiscard(gothmog, gear2, warwarg, hisRelentless);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var vicious = scn.GetShadowCard("vicious");
		var effort = scn.GetShadowCard("effort");
		var goblinrunner = scn.GetShadowCard("goblinrunner");

		scn.MoveCardsToHand(goblinrunner);
		scn.MoveCardsToDiscard(vicious);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.SetTwilight(10);
		assertInHand(goblinrunner);
		assertInDiscard(vicious);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rider));

		scn.MoveCardsToHand(effort);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(rider));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcMountInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var wargrider = scn.GetShadowCard("wargrider");
		var vicious = scn.GetShadowCard("vicious");
		var warwarg = scn.GetShadowCard("warwarg");
		var gear2 = scn.GetShadowCard("gear2");
		var effort = scn.GetShadowCard("effort");

		scn.MoveCardsToHand(effort);
		// An [orc] possession that is no mount, and a mount of another culture that the Warg-rider could bear.
		scn.MoveCardsToDiscard(gear2, warwarg);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, wargrider);
		scn.SetTwilight(10);
		assertInHand(effort);
		assertInDiscard(gear2, warwarg);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rider));

		scn.MoveCardsToDiscard(vicious);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(rider));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithThePoolShortOfTheMountsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetShadowCard("rider");
		var vicious = scn.GetShadowCard("vicious");
		var effort = scn.GetShadowCard("effort");

		scn.MoveCardsToHand(effort);
		scn.MoveCardsToDiscard(vicious);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		assertInHand(effort);
		assertInDiscard(vicious);
		scn.FreepsPass();

		// The Vicious Warg costs 3: playing it is part of the price.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rider));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(rider));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
