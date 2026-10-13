package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("albert", "1_69");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] condition, support area; its text wants an Uruk-hai
					put("throw", "2_71");       // Throw Yourself in Next Time: [moria] condition borne by a Hobbit companion
					put("defense", "1_244");    // Desperate Defense of the Ring: [sauron] condition borne by a companion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] possession borne by a [moria] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AlbertDrearyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Albert Dreary, Entertainer from Bree
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 1
		 * Game Text: To play, spot Gandalf.
		 * Maneuver: Exert Albert Dreary to discard an [isengard] or [moria] condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("albert");

		assertEquals("Albert Dreary", card.getBlueprint().getTitle());
		assertEquals("Entertainer from Bree", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var albert = scn.GetFreepsCard("albert");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(albert, gandalf);

		scn.StartGame();

		// The Ent Avenger is a [gandalf] companion, and no Gandalf.
		assertFalse(scn.FreepsPlayAvailable(albert));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(albert));

		scn.FreepsPlayCard(albert);
		assertInZone(Zone.SUPPORT, albert);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionExertsAlbertToDiscardAnIsengardOrMoriaCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var albert = scn.GetFreepsCard("albert");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(albert);
		scn.MoveCompanionsToTable(gandalf);

		var armory = scn.GetShadowCard("armory");
		var thrw = scn.GetShadowCard("throw");
		var defense = scn.GetShadowCard("defense");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToSupportArea(armory);
		scn.AttachCardsTo(frodo, thrw);
		scn.AttachCardsTo(gandalf, defense);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, scimitar);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(albert));
		assertTrue(scn.FreepsActionAvailable(albert));
		scn.FreepsUseCardAction(albert);
		assertEquals(1, scn.GetWoundsOn(albert));
		// An [isengard] condition in the support area and a [moria] one borne by Frodo; not the [moria] possession,
		// the [moria] minion or the [sauron] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, thrw));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scimitar, troop, defense));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(armory);
		assertInZone(Zone.DISCARD, armory);
		assertAttachedTo(thrw, frodo);
		assertAttachedTo(defense, gandalf);
		assertAttachedTo(scimitar, troop);

		scn.ShadowPass();
		// Used again, with one [moria] condition left to discard: it goes unasked.
		scn.FreepsUseCardAction(albert);
		assertEquals(2, scn.GetWoundsOn(albert));
		assertInZone(Zone.DISCARD, thrw);
		assertAttachedTo(defense, gandalf);
		assertAttachedTo(scimitar, troop);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedOnceAlbertIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var albert = scn.GetFreepsCard("albert");
		scn.MoveAlliesToTable(albert);
		scn.AddWoundsToChar(albert, 2);

		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(armory);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 3 with 2 wounds: he cannot exert, though an [isengard] condition is there to discard.
		assertEquals(2, scn.GetWoundsOn(albert));
		assertInZone(Zone.SUPPORT, armory);
		assertFalse(scn.FreepsActionAvailable(albert));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
