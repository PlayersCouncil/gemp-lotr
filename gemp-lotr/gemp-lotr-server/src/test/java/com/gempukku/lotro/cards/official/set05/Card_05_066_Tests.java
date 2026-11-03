package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("master", "5_66");
					put("master2", "5_66");
					// Two [isengard] mounts for the discard pile, bearer must be a warg-rider: War-warg, cost 4 (a skirmish
					// Response), and Hound of Isengard, cost 2 (its own play trigger wants an [isengard] skirmish event in
					// the discard pile, and there is none)
					put("warwarg", "5_64");
					put("hound", "102_24");
					// Sharku's Warg: a unique [isengard] mount, cost 5, laid in the Free Peoples player's own discard pile
					put("sharkuswarg", "5_59");
					// Uruk-hai Sword: an [isengard] possession that is no mount, cost 1, bearer an Uruk-hai
					put("sword", "1_160");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai who could bear the Sword; damage +1 only
					// Threatening Warg: an [orc] mount, cost 2, bearer an [orc] Orc of strength 10 or less; its play trigger
					// wants 2 such Orcs, and there is one
					put("threatening", "17_91");
					// Denizen of the Black Pit: an [orc] Orc of strength 10, who could bear the Threatening Warg; +2 only
					// underground (site 3 is not)
					put("denizen", "11_117");
					put("rider", "5_53");       // Isengard Rider: another warg-rider, strength 8, vitality 3; fierce while not exhausted
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WargmasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Warg-master
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Warg-rider</b>. When you play this minion, you may play an [isengard] mount from your discard pile.<br>While this minion is not exhausted, he is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("master");

		assertEquals("Warg-master", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WARG_RIDER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedHeMayPlayAnIsengardMountFromHisOwnersDiscardPileOnAnyWargRider() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var warwarg = scn.GetShadowCard("warwarg");
		var hound = scn.GetShadowCard("hound");
		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");
		var threatening = scn.GetShadowCard("threatening");
		var denizen = scn.GetShadowCard("denizen");
		var rider = scn.GetShadowCard("rider");
		var sharkuswarg = scn.GetFreepsCard("sharkuswarg");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(master);
		scn.MoveCardsToDiscard(warwarg, hound, sword, threatening, sharkuswarg);
		assertInDiscard(warwarg, hound, sword, threatening, sharkuswarg);
		scn.MoveMinionsToTable(savage, denizen, rider);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInHand(master);
		assertInDiscard(warwarg, hound, sword, threatening, sharkuswarg);

		scn.ShadowPlayCard(master);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(master));
		scn.ShadowAcceptOptionalTrigger();

		// Either [isengard] mount; not an [isengard] possession that is no mount (the Savage could bear it), not an
		// [orc] mount (the Denizen could bear it), not the Free Peoples player's own [isengard] mount.  The "may" was
		// the offer's: the mount itself is asked for.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(warwarg, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, threatening, sharkuswarg));
		scn.ShadowChooseCard(warwarg);

		// The mount goes on any warg-rider, not only on him.
		assertTrue(scn.ShadowHasCardChoiceAvailable(master, rider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, denizen));
		scn.ShadowChooseCard(rider);

		assertAttachedTo(warwarg, rider);
		assertInDiscard(hound, sword, threatening, sharkuswarg);
		assertInZone(Zone.SHADOW_CHARACTERS, master);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheMountInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var warwarg = scn.GetShadowCard("warwarg");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(master);
		scn.MoveCardsToDiscard(warwarg);
		assertInDiscard(warwarg);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInDiscard(warwarg);

		scn.ShadowPlayCard(master);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(master));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, master);
		assertInDiscard(warwarg);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedWithNoIsengardMountInHisOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var sword = scn.GetShadowCard("sword");
		var savage = scn.GetShadowCard("savage");
		var threatening = scn.GetShadowCard("threatening");
		var denizen = scn.GetShadowCard("denizen");
		var sharkuswarg = scn.GetFreepsCard("sharkuswarg");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(master);
		scn.MoveCardsToDiscard(sword, threatening, sharkuswarg);
		assertInDiscard(sword, threatening, sharkuswarg);
		scn.MoveMinionsToTable(savage, denizen);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInDiscard(sword, threatening, sharkuswarg);

		scn.ShadowPlayCard(master);
		assertInZone(Zone.SHADOW_CHARACTERS, master);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(master));
		assertInDiscard(sword, threatening, sharkuswarg);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenThePoolLeftCannotPayForTheMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var warwarg = scn.GetShadowCard("warwarg");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(master);
		scn.MoveCardsToDiscard(warwarg);
		assertInDiscard(warwarg);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 2 for him + 2 roaming at site 3 leaves 3, one short of the War-warg's 4.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertInDiscard(warwarg);

		scn.ShadowPlayCard(master);
		assertInZone(Zone.SHADOW_CHARACTERS, master);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(master));
		assertInDiscard(warwarg);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondWargmasterPlayedOffersItsOwnPlayAndTheFirstOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var master2 = scn.GetShadowCard("master2");
		var warwarg = scn.GetShadowCard("warwarg");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(master2);
		scn.MoveCardsToDiscard(warwarg);
		assertInDiscard(warwarg);
		scn.MoveMinionsToTable(master);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInDiscard(warwarg);

		// Another warg-rider, an [isengard] Orc of his own title, played after him: only the new copy's own play is answered.
		scn.ShadowPlayCard(master2);
		assertInZone(Zone.SHADOW_CHARACTERS, master2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(master2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(master));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(warwarg);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExhaustedHeIsNotFierceUntilAWarWargPlayedOnHimLeavesHimMoreThanOneVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var master = scn.GetShadowCard("master");
		var master2 = scn.GetShadowCard("master2");
		var warwarg = scn.GetShadowCard("warwarg");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(warwarg);
		assertInHand(warwarg);

		scn.StartGame();
		scn.MoveMinionsToTable(master, master2, troop);
		// Vitality 2 and no wound: 2 left, one above exhausted, and fierce.
		assertEquals(0, scn.GetWoundsOn(master));
		assertEquals(2, scn.GetVitality(master));
		assertFalse(scn.IsExhausted(master));
		assertTrue(scn.HasKeyword(master, Keyword.FIERCE));
		scn.AddWoundsToChar(master, 1);
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(0, scn.GetWoundsOn(master2));
		scn.FreepsPass();

		// Vitality 2 with 1 wound: exhausted, and not fierce.  The other copy goes by its own state, no wound and
		// fierce; the Troop, not exhausted, is not fierce either.
		assertEquals(1, scn.GetWoundsOn(master));
		assertTrue(scn.IsExhausted(master));
		assertFalse(scn.HasKeyword(master, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(master2));
		assertFalse(scn.IsExhausted(master2));
		assertTrue(scn.HasKeyword(master2, Keyword.FIERCE));
		assertFalse(scn.IsExhausted(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		scn.ShadowPlayCard(warwarg);
		scn.ShadowChooseCard(master);
		assertAttachedTo(warwarg, master);

		// 2 + 2 less 1 wound: 3 left, not exhausted, fierce.
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(3, scn.GetVitality(master));
		assertFalse(scn.IsExhausted(master));
		assertTrue(scn.HasKeyword(master, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(master2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
