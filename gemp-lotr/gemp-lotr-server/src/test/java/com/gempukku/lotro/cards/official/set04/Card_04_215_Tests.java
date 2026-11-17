package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_215_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wounded1", "4_215");
					put("wounded2", "4_215");
					// Gríma, Wormtongue: an [isengard] Man, vitality 3; a maneuver ability never used.
					put("grima", "4_154");
					// Rohirrim Traitor: an [isengard] Man, vitality 2; strength +2 while a companion bears an
					// [isengard] condition.
					put("traitor", "6_72");
					// Uruk Savage: an [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text.
					put("savage", "1_151");
					// Dunlending Footmen: a [dunland] Man, vitality 2; a cost modifier in skirmishes only.
					put("footmen", "6_3");
					// Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability never used.
					put("aragorn", "1_89");
					// Boromir, Son of Denethor: a [gondor] Man companion, vitality 3; a skirmish ability never used.
					put("boromir", "1_97");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion; +2 only while he bears a weapon.
					put("merry", "1_303");
					// Guma, Plains Farmer: a [rohan] Man ally, vitality 2, home 4T; discarded only if an opponent
					// controls that site, which is on no path here; a fellowship heal of [rohan] Men never used.
					put("guma", "4_277");
					// Elrond, Lord of Rivendell: spotted for Master of Healing; his own start-of-turn heal reaches
					// allies whose home is site 3 only.
					put("elrond", "1_40");
					// Master of Healing: "Fellowship or Regroup: Spot Elrond (or 2 [elven] allies) to heal a
					// companion or ally."
					put("healing1", "3_22");
					put("healing2", "3_22");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WoundedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Wounded
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, exert an [isengard] Man. Plays on a Free Peoples Man. When you play this condition, exert bearer.<br>Wounds may not be removed from bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wounded1");

		assertEquals("Wounded", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedByExertingAnIsengardManOnAFreePeoplesManWhoThenExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wounded = scn.GetShadowCard("wounded1");
		var grima = scn.GetShadowCard("grima");
		var traitor = scn.GetShadowCard("traitor");
		var savage = scn.GetShadowCard("savage");
		var footmen = scn.GetShadowCard("footmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(aragorn, boromir, merry);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(wounded);
		assertInZone(Zone.HAND, wounded);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima, traitor, savage, footmen);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.HAND, wounded);
		assertTrue(scn.ShadowPlayAvailable(wounded));
		scn.ShadowPlayCard(wounded);

		// The bearer is chosen first: Free Peoples Men, companions and an ally of another culture; not a Free
		// Peoples Hobbit, nor the Shadow player's own Man.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, boromir, guma));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(merry, grima));
		scn.ShadowChooseCard(aragorn);

		// Then the cost: two [isengard] Men of different titles; not an [isengard] Uruk-hai, nor a [dunland]
		// Man, though both could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(grima, traitor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, footmen));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(traitor));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(traitor);

		assertAttachedTo(wounded, aragorn);
		assertEquals(1, scn.GetWoundsOn(traitor));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(footmen));
		// Its play exerts its bearer, and no other Free Peoples Man.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyIsengardManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wounded = scn.GetShadowCard("wounded1");
		var grima = scn.GetShadowCard("grima");
		var savage = scn.GetShadowCard("savage");
		var footmen = scn.GetShadowCard("footmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wounded);
		assertInZone(Zone.HAND, wounded);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima, savage, footmen);
		// Vitality 3 with 2 wounds: Gríma cannot exert.  The Uruk-hai and the [dunland] Man could.
		scn.AddWoundsToChar(grima, 2);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(grima));
		assertInZone(Zone.HAND, wounded);
		assertFalse(scn.ShadowPlayAvailable(wounded));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedOnAnExhaustedManExertsNeitherHimNorTheFirstBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wounded1 = scn.GetShadowCard("wounded1");
		var wounded2 = scn.GetShadowCard("wounded2");
		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, wounded1);
		scn.MoveCardsToHand(wounded2);
		assertAttachedTo(wounded1, aragorn);
		assertInZone(Zone.HAND, wounded2);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Boromir is exhausted.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(wounded2);
		scn.ShadowChooseCard(boromir);

		// Gríma, the one [isengard] Man, pays unasked.  The copy played exerts its own bearer, who cannot exert
		// and is not wounded instead (a wound would kill him); the copy already on Aragorn does not answer it.
		assertAttachedTo(wounded2, boromir);
		assertEquals(1, scn.GetWoundsOn(grima));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WoundsAreNotHealedOnItsBearerAndAreOnOtherCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wounded1 = scn.GetShadowCard("wounded1");
		var wounded2 = scn.GetShadowCard("wounded2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var healing2 = scn.GetFreepsCard("healing2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(guma, elrond);
		scn.MoveCardsToHand(healing1, healing2);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(guma, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(guma));

		// With the card in no one's way, Aragorn is healed like anyone else.
		scn.FreepsPlayCard(healing1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, guma, frodo));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// A companion and an ally each bear a copy.
		scn.AttachCardsTo(aragorn, wounded1);
		scn.AttachCardsTo(guma, wounded2);
		scn.FreepsRefreshDecision();
		assertAttachedTo(wounded1, aragorn);
		assertAttachedTo(wounded2, guma);

		// Borne, it keeps their wounds on them; the other wounded characters may still be healed.
		scn.FreepsPlayCard(healing2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guma));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
