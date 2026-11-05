package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_197_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stalker", "4_197");
					put("stalker2", "4_197");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					// Tower Assassin: [sauron] Orc, strength 11, vitality 3.  "Maneuver: Exert Tower Assassin to
					// wound an ally."
					put("assassin", "2_93");

					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: [elven] Elf ally, home 6; a fellowship ability only
					// Rosie Cotton, Hobbiton Lass: [shire] Hobbit ally, vitality 2, home 1.  Her texts are about Sam.
					put("rosie", "1_309");
					// Elrond, Lord of Rivendell: [elven] ally, vitality 4, home 3.  At the start of his owner's turn
					// he heals allies whose home is site 3, so he is laid after the game starts.
					put("elrond", "1_40");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukStalkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Stalker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While an ally is in the dead pile, this minion is strength +3 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stalker");

		assertEquals("Uruk Stalker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukStalkerIsStrengthPlus3AndFierceWhileAllyIsInDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var rosie = scn.GetFreepsCard("rosie");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(celeborn);

		var stalker = scn.GetShadowCard("stalker");
		var stalker2 = scn.GetShadowCard("stalker2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(stalker, stalker2, savage);
		// A companion in the dead pile, an ally in the discard pile and an ally in play: none of them is an
		// ally in the dead pile.
		scn.MoveCardsToDeadPile(aragorn);
		scn.MoveCardsToDiscard(elrond);
		scn.MoveCardsToSupportArea(rosie);

		assertInZone(Zone.HAND, celeborn);
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.DISCARD, elrond);
		assertInZone(Zone.SUPPORT, rosie);
		assertEquals(8, scn.GetStrength(stalker));
		assertFalse(scn.HasKeyword(stalker, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(stalker2));
		assertFalse(scn.HasKeyword(stalker2, Keyword.FIERCE));

		scn.MoveCardsToDeadPile(celeborn);

		assertInZone(Zone.DEAD, celeborn);
		assertEquals(11, scn.GetStrength(stalker));
		assertTrue(scn.HasKeyword(stalker, Keyword.FIERCE));
		// Each copy gets its own bonus once: two copies do not give one of them +6.
		assertEquals(11, scn.GetStrength(stalker2));
		assertTrue(scn.HasKeyword(stalker2, Keyword.FIERCE));
		// Another [isengard] Uruk-hai is not "this minion".
		assertEquals(5, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		// A second ally there, of another culture and race, adds nothing more.
		scn.MoveCardsToDeadPile(rosie);
		assertInZone(Zone.DEAD, rosie);
		assertEquals(11, scn.GetStrength(stalker));
		assertEquals(11, scn.GetStrength(stalker2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyWoundedToDeathMakesTheStalkerStrongerAndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToSupportArea(rosie);

		var stalker = scn.GetShadowCard("stalker");
		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveCardsToSupportArea(elrond);
		scn.MoveMinionsToTable(stalker, assassin);
		// Vitality 2 with 1 wound: the next wound kills her.
		scn.AddWoundsToChar(rosie, 1);
		assertInZone(Zone.SUPPORT, rosie, elrond);
		assertEquals(1, scn.GetWoundsOn(rosie));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Two allies in play, one of them wounded: no ally is in the dead pile yet.
		assertEquals(8, scn.GetStrength(stalker));
		assertFalse(scn.HasKeyword(stalker, Keyword.FIERCE));

		scn.ShadowUseCardAction(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(rosie, elrond));
		scn.ShadowChooseCard(rosie);

		assertInZone(Zone.DEAD, rosie);
		assertInZone(Zone.SUPPORT, elrond);
		assertEquals(11, scn.GetStrength(stalker));
		assertTrue(scn.HasKeyword(stalker, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnAllyInTheShadowPlayersOwnDeadPileDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The Shadow player's own ally, killed on one of his own turns.
		var shadowCeleborn = scn.GetShadowCard("celeborn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var stalker = scn.GetShadowCard("stalker");

		scn.StartGame();
		scn.MoveMinionsToTable(stalker);
		scn.MoveCardsToDeadPile(shadowCeleborn);

		assertInZone(Zone.DEAD, shadowCeleborn);
		assertEquals(8, scn.GetStrength(stalker));
		assertFalse(scn.HasKeyword(stalker, Keyword.FIERCE));

		// The same ally in the Free Peoples player's dead pile does.
		scn.MoveCardsToDeadPile(celeborn);
		assertInZone(Zone.DEAD, celeborn);
		assertEquals(11, scn.GetStrength(stalker));
		assertTrue(scn.HasKeyword(stalker, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
