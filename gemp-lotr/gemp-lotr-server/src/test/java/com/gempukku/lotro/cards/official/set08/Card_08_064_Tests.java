package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mumakil", "8_64");

					put("warrior", "4_222");    // Desert Warrior: [raider] Southron Man; a start-of-skirmish trigger that costs (3)
					put("scout", "4_252");      // Southron Scout: [raider] Southron Man, Ambush (2), nothing else
					put("attacker", "7_141");   // Easterling Attacker: [raider] Easterling Man, no Southron; a Skirmish ability only

					put("gimli", "1_13");       // Gimli, Son of Gloin, Boromir, Son of Denethor and Gandalf, Friend of the Shirefolk:
					put("boromir", "1_97");     // companions, so that 4 threats can be added
					put("gandalf", "1_72");
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] support condition of cost 0, no requirement

					// Cards for the Free Peoples player's hand, which sets who has initiative.
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Mûmakil
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 3
		 * Game Text: Bearer must be a Southron.<br>Bearer is <b>fierce</b>.<br>While you have initiative or can spot 4 threats, bearer is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mumakil");

		assertEquals("Mûmakil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnASouthronOnlyAndMakesHimFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumakil = scn.GetShadowCard("mumakil");
		var warrior = scn.GetShadowCard("warrior");
		var scout = scn.GetShadowCard("scout");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToHand(mumakil);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, scout, attacker);
		scn.SetTwilight(10);
		assertInHand(mumakil);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));

		scn.ShadowPlayCard(mumakil);
		// Two Southrons may bear it; the Easterling may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, scout));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attacker));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(warrior);

		assertInZone(Zone.ATTACHED, mumakil);
		assertSame(warrior, mumakil.getAttachedTo());
		assertTrue(scn.HasKeyword(warrior, Keyword.FIERCE));
		// The other Southron is not made fierce.
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsDamagePlus1OnceTheShadowHasTheInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));

		var mumakil = scn.GetShadowCard("mumakil");
		var warrior = scn.GetShadowCard("warrior");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, scout);
		scn.AttachCardsTo(warrior, mumakil);
		scn.FreepsRefreshDecision();

		assertSame(warrior, mumakil.getAttachedTo());
		assertEquals(0, scn.GetThreats());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(scout, Keyword.DAMAGE));

		// The play takes the Free Peoples hand from four to three.
		scn.FreepsPlayCard(stairs);
		assertInZone(Zone.SUPPORT, stairs);

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		// The other Southron gets nothing.
		assertEquals(0, scn.GetKeywordCount(scout, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTheInitiativeBearerIsDamagePlus1AtFourThreatsAndNotAtThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("gimli", "boromir", "gandalf");
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		var mumakil = scn.GetShadowCard("mumakil");
		var warrior = scn.GetShadowCard("warrior");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, scout);
		scn.AttachCardsTo(warrior, mumakil);
		scn.AddThreats(3);
		// A burden, which is no threat.
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		assertSame(warrior, mumakil.getAttachedTo());
		assertEquals(3, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DAMAGE));

		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(scout, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
