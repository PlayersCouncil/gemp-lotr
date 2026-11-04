package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("desolation", "15_107");

					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] support-area condition; nothing below 5 burdens
					put("threatening", "13_103"); // Always Threatening: [orc] support-area condition, cost 1; an optional token trigger on its own play, a skirmish ability
					put("blow", "15_115");       // Pummeling Blow: [orc] condition borne by a site
					put("sbof", "1_278");        // Strength Born of Fear: [sauron] support-area condition, a skirmish ability only
					put("grond", "18_82");       // Grond, Forged With Black Steel: [orc] support-area possession; a regroup-move trigger only
					put("gollum", "5_24");       // Gollum, Nasty Treacherous Creature: a Shadow [gollum] minion; a regroup ability only

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability only
					put("gimli", "1_13");        // Gimli, Son of Gloin: [dwarven] companion; a skirmish ability only
					put("stairs", "1_24");       // Stairs of Khazad-dum: a [dwarven] support-area condition; strength +1 for a Dwarf against a [moria] minion only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesolationOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Desolation Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot 3 Free Peoples cultures or discard 2 [orc] conditions from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("desolation");

		assertEquals("Desolation Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilASecondOrcConditionIsPlayedAndThenDiscardsBothUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var desolation = scn.GetShadowCard("desolation");
		var beckoning = scn.GetShadowCard("beckoning");
		var threatening = scn.GetShadowCard("threatening");
		var sbof = scn.GetShadowCard("sbof");
		var grond = scn.GetShadowCard("grond");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(beckoning, sbof, grond);
		scn.MoveCardsToHand(desolation, threatening);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertInZone(Zone.SUPPORT, beckoning, sbof, grond);
		assertInHand(desolation, threatening);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// [shire] Frodo and [gondor] Aragorn beside a Shadow [gollum] card: two Free Peoples cultures.  One [orc]
		// condition, beside a [sauron] condition and an [orc] possession.
		assertEquals(2, scn.GetFreepsCultureCount());
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(desolation));

		scn.ShadowPlayCard(threatening);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(threatening));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, threatening);
		assertTrue(scn.ShadowPlayAvailable(desolation));

		// The spot cannot be made, and only two [orc] conditions are there: both go, nothing asked.
		scn.ShadowPlayCard(desolation);
		assertInZone(Zone.SHADOW_CHARACTERS, desolation);
		assertInDiscard(beckoning, threatening);
		assertInZone(Zone.SUPPORT, sbof, grond);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDiscardIsOfTwoOrcConditionsFromPlayAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var desolation = scn.GetShadowCard("desolation");
		var beckoning = scn.GetShadowCard("beckoning");
		var threatening = scn.GetShadowCard("threatening");
		var blow = scn.GetShadowCard("blow");
		var sbof = scn.GetShadowCard("sbof");
		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(beckoning, threatening, sbof, grond);
		scn.MoveCardsToHand(desolation);

		scn.StartGame();
		// Pummeling Blow on site 1, which is still in play at site 2.
		scn.AttachCardsTo(scn.GetCurrentSite(), blow);
		assertInZone(Zone.SUPPORT, beckoning, threatening, sbof, grond);
		assertInZone(Zone.ATTACHED, blow);
		assertInHand(desolation);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetFreepsCultureCount());
		scn.ShadowPlayCard(desolation);
		assertTrue(scn.ShadowHasCardChoiceAvailable(beckoning, threatening, blow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sbof, grond));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(beckoning, blow);

		assertInZone(Zone.SHADOW_CHARACTERS, desolation);
		assertInDiscard(beckoning, blow);
		assertInZone(Zone.SUPPORT, threatening, sbof, grond);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeFreePeoplesCulturesOneOfThemOnAConditionItIsPlayedWithNothingDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToSupportArea(stairs);

		var desolation = scn.GetShadowCard("desolation");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveCardsToHand(desolation);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stairs, beckoning);
		assertInHand(desolation);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// [shire] Frodo, [gondor] Aragorn and the [dwarven] Stairs; one [orc] condition only.
		assertEquals(3, scn.GetFreepsCultureCount());
		assertTrue(scn.ShadowPlayAvailable(desolation));
		scn.ShadowPlayCard(desolation);
		assertInZone(Zone.SHADOW_CHARACTERS, desolation);
		assertInZone(Zone.SUPPORT, beckoning, stairs);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithBothAlternativesPossibleThePlayerIsAskedWhichAndMayDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));

		var desolation = scn.GetShadowCard("desolation");
		var beckoning = scn.GetShadowCard("beckoning");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(beckoning, threatening);
		scn.MoveCardsToHand(desolation);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beckoning, threatening);
		assertInHand(desolation);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// [shire], [gondor] and [dwarven] companions, and two [orc] conditions.
		assertEquals(3, scn.GetFreepsCultureCount());
		scn.ShadowPlayCard(desolation);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Spot 3"));
		assertTrue(scn.ShadowChoiceAvailable("Discard 2"));
		scn.ShadowChooseOption("Discard 2");

		assertInZone(Zone.SHADOW_CHARACTERS, desolation);
		assertInDiscard(beckoning, threatening);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
