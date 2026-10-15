package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horde", "5_50");
					put("hound", "102_24");     // Hound of Isengard: [isengard] mount on a warg-rider, strength +1, vitality +1;
					                            // its "when you play" take needs an [isengard] skirmish event in the discard pile
					put("hound2", "102_24");
					put("rider", "5_53");       // Isengard Rider: warg-rider, strength 8, vitality 3; fierce while not exhausted
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoulHordeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Foul Horde
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Warg-rider</b>.
		 * While Foul Horde is not exhausted, it is <b>fierce</b>.
		 * While Foul Horde has at least 3 vitality, it is <b>ambush</b> (1).
		 * While Foul Horde has at least 4 vitality, it is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horde");

		assertEquals("Foul Horde", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WARG_RIDER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AHoundOfIsengardPlayedOnItUnwoundedBringsItTo4VitalityAndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var hound = scn.GetShadowCard("hound");
		var hound2 = scn.GetShadowCard("hound2");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCardsToHand(hound);
		assertInHand(hound);

		scn.StartGame();
		scn.SetTwilight(2);
		// Laid before the Shadow phase begins, so that its first choice of actions takes them in.
		scn.MoveMinionsToTable(horde, rider);
		// Another warg-rider at 4 vitality: 3 and the Hound's +1.
		scn.AttachCardsTo(rider, hound2);
		assertAttachedTo(hound2, rider);
		scn.FreepsPass();

		// Its printed 3: one short of damage +1, and ambush and fierce.
		assertEquals(3, scn.GetVitality(horde));
		assertEquals(0, scn.GetKeywordCount(horde, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertEquals(4, scn.GetVitality(rider));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.AMBUSH));

		// The Isengard Rider bears a mount already: Foul Horde is the one bearer left.
		scn.ShadowPlayCard(hound);
		assertAttachedTo(hound, horde);

		assertEquals(4, scn.GetVitality(horde));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		// The other warg-rider at 4 vitality gets nothing from it.
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.AMBUSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneWoundItIsNotAmbushUntilAHoundOfIsengardBringsItBackTo3Vitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var hound = scn.GetShadowCard("hound");
		var hound2 = scn.GetShadowCard("hound2");
		var rider = scn.GetShadowCard("rider");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hound);
		assertInHand(hound);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.MoveMinionsToTable(horde, rider, troop);
		scn.AttachCardsTo(rider, hound2);
		assertAttachedTo(hound2, rider);
		scn.AddWoundsToChar(horde, 1);
		assertEquals(1, scn.GetWoundsOn(horde));
		scn.FreepsPass();

		// 3 less 1 wound: one short of ambush, and still fierce.
		assertEquals(2, scn.GetVitality(horde));
		assertEquals(0, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(horde, Keyword.DAMAGE));

		scn.ShadowPlayCard(hound);
		assertAttachedTo(hound, horde);

		// 3 + 1 less 1 wound: exactly 3, ambush (1); still one short of damage +1.
		assertEquals(1, scn.GetWoundsOn(horde));
		assertEquals(3, scn.GetVitality(horde));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertEquals(0, scn.GetKeywordCount(horde, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		// Other minions with 3 vitality or more get no ambush from it.
		assertEquals(3, scn.GetVitality(troop));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.AMBUSH));
		assertEquals(4, scn.GetVitality(rider));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.AMBUSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExhaustedItIsNotFierceUntilAHoundOfIsengardBringsItTo2Vitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hound);
		assertInHand(hound);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.MoveMinionsToTable(horde, troop);
		scn.AddWoundsToChar(horde, 2);
		assertEquals(2, scn.GetWoundsOn(horde));
		scn.FreepsPass();

		// 3 less 2 wounds: exhausted, and not fierce.
		assertEquals(1, scn.GetVitality(horde));
		assertTrue(scn.IsExhausted(horde));
		assertFalse(scn.HasKeyword(horde, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		// Another minion, not exhausted, and not fierce.
		assertFalse(scn.IsExhausted(troop));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		scn.ShadowPlayCard(hound);
		assertAttachedTo(hound, horde);

		// 3 + 1 less 2 wounds: 2 left, not exhausted, fierce; still one short of ambush.
		assertEquals(2, scn.GetWoundsOn(horde));
		assertEquals(2, scn.GetVitality(horde));
		assertFalse(scn.IsExhausted(horde));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
